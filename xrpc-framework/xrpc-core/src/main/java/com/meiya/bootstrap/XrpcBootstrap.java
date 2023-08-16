package com.meiya.bootstrap;

import com.meiya.annotation.XrpcApi;
import com.meiya.channelhandler.ProviderChannelInitializer;
import com.meiya.config.ReferenceConfig;
import com.meiya.config.RegistryConfig;
import com.meiya.config.ServiceConfig;
import com.meiya.config.XrpcBootstrapConfiguration;
import com.meiya.detection.HeartbeatDetector;
import com.meiya.exceptions.NettyException;
import com.meiya.hook.XrpcShutDownHook;
import com.meiya.loadbalancer.LoadBalancer;
import com.meiya.protection.CircuitBreaker;
import com.meiya.protection.CurrentLimiter;
import com.meiya.transport.message.XrpcRequest;
import com.meiya.utils.FileUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;


import java.lang.reflect.InvocationTargetException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author xiaopf
 */
@Slf4j
public class XrpcBootstrap {


    /**
     * 配置中心
     */
    @Getter
    private final XrpcBootstrapConfiguration configuration;
    /**
     * ip级别的限流器缓存
     */
    public static final Map<InetSocketAddress, CurrentLimiter> IP_CURRENT_LIMITER_CACHE = new ConcurrentHashMap<>(16);
    /**
     * ip级别的熔断器缓存
     */
    public static final Map<InetSocketAddress, CircuitBreaker> IP_CIRCUIT_BREAKER_CACHE = new ConcurrentHashMap<>(16);
    /**
     * 记录所有需要发现的服务对应的主机
     * 去重后
     */
    public static List<InetSocketAddress> ALL_SERVICE_ADDRESS_LIST = new ArrayList<>();
    /**
     * 记录所有需要发现的服务
     */
    private static final List<ReferenceConfig<?>> REFERENCE_CONFIG_LIST = new ArrayList<>();
    /**
     * 不同主机的响应时长
     */
    public static final TreeMap<Long, List<Channel>> RESPONSE_TIME_CHANNEL_CACHE = new TreeMap<>();

    /**
     * threadLocal
     */
    public static final ThreadLocal<XrpcRequest> REQUEST_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 全局对外挂起的completableFuture
     * key为请求的唯一标识
     */
    public static final Map<Long, CompletableFuture<Object>> PENDING_REQUEST = new ConcurrentHashMap<>(128);


    /**
     * 连接缓存
     * 使用类作为key 要注意该类是否重写了hashcode和equals方法
     */
    public static final Map<InetSocketAddress, Channel> CHANNEL_CACHE = new ConcurrentHashMap<>(16);
    /**
     * 服务列表 用于反射调用获得结果 类似保存了所有发布的服务的实例对象的一个容器
     * key--> interface的全限名 value--> ServiceConfig
     */
    public static final Map<String, ServiceConfig<?>> SERVICE_MAP = new ConcurrentHashMap<>(16);
    /**
     * XrpcBootstrap是单例 采用饿汉式方法创建
     */
    private static final XrpcBootstrap XRPC_BOOTSTRAP = new XrpcBootstrap();

    private XrpcBootstrap() {
        //初始化 加载配置
        configuration = new XrpcBootstrapConfiguration();
    }

    public static XrpcBootstrap getInstance() {
        return XRPC_BOOTSTRAP;
    }


    /**
     * 定义当前应用的名称
     *
     * @param applicationName 应用名称
     * @return 当前实例
     */
    public XrpcBootstrap application(String applicationName) {
        configuration.setApplicationName(applicationName);
        return this;
    }

    /**
     * 配置注册中心
     *
     * @param registryConfig 注册中心的封装
     * @return 当前实例
     */
    public XrpcBootstrap registry(RegistryConfig registryConfig) {
        configuration.setRegistryConfig(registryConfig);
        configuration.setRegistry(configuration.getRegistryConfig().getRegistry());
        return this;
    }


    /**
     * 服务提供方完成配置 启动netty服务
     */
    public void start() {
        //1.注册jvm关闭钩子函数
        Runtime.getRuntime().addShutdownHook(new XrpcShutDownHook());
        //2.启动netty
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        NioEventLoopGroup boss = new NioEventLoopGroup(2);
        NioEventLoopGroup worker = new NioEventLoopGroup(10);

        try {
            ChannelFuture channelFuture = serverBootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ProviderChannelInitializer())
                    .bind(configuration.getPort())
                    .sync();
            Channel channel = channelFuture.channel();
            channel.closeFuture().addListener(future -> {
                log.info("服务端netty关闭");
                try {
                    boss.shutdownGracefully().sync();
                    worker.shutdownGracefully().sync();
                } catch (Exception e) {
                    log.info("服务端netty优雅关闭时发生异常！");
                    throw new NettyException(e);
                }
            });
        } catch (InterruptedException e) {
            log.info("服务端netty启动时发生异常!");
            throw new NettyException(e);
        }
    }


    //----------------------------------provider相关的api--------------------------------------
    /**
     * 包扫描 发布服务
     * @param packageName 包名称
     * @return 实例对象
     */
    public XrpcBootstrap scan(String packageName) {
        //根据包名获取其下类全限定名
        List<String> allClassNames = FileUtils.getAllClassNamesByPackageName(packageName);
        //筛选标识了XrpcApi注解的实现类，获得其Class对象
        List<? extends Class<?>> classes = allClassNames.stream()
                .map(className -> {
                    try {
                        return Class.forName(className);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                })
                .filter(clazz -> clazz.getAnnotation(XrpcApi.class) != null)
                .toList();
        //获取实现类Class对象的所有接口（一个实现类可能有多个接口）
        //获取实现类Class对象的一个实例
        for (Class<?> clazz : classes) {
            Class<?>[] interfaces = clazz.getInterfaces();
            Object instance = null;
            try {
                //这边只支持无参构造器 通过Class对象获得一个类的实例
                instance = clazz.getConstructor().newInstance();
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                     NoSuchMethodException e) {
                throw new RuntimeException(e);
            }

            //获取实现类对应的分组
            XrpcApi annotation = clazz.getAnnotation(XrpcApi.class);
            String group = annotation.group();
            //封装serviceConfig对象 发布
            for (Class<?> anInterface : interfaces) {
                ServiceConfig<?> serviceConfig = new ServiceConfig<>();
                serviceConfig.setInterface(anInterface);
                serviceConfig.setRef(instance);
                serviceConfig.setGroup(group);
                //发布服务
                this.publish(serviceConfig);
                if (log.isDebugEnabled()) {
                    log.info("通过包扫描发布了服务【{}】",anInterface);
                }
            }
        }
        return this;
    }

    /**
     * 服务发布 配合扫包使用
     *
     * @param serviceConfig 需要发布的服务的封装
     */
    private void publish(ServiceConfig<?> serviceConfig) {
        //将服务注册到注册中心上
        configuration.getRegistry().register(serviceConfig);
        //维护服务列表 一个接口只维护一个实现类的实例 这里不考虑一个接口有多个实现类
        SERVICE_MAP.put(serviceConfig.getInterface().getName(), serviceConfig);
    }



    //----------------------------------consumer相关的api--------------------------------------


    /**
     * 配置负载均衡策略
     * @param loadBalancer 负载均衡策略
     * @return 当前实例
     */
    public XrpcBootstrap loadBalance(LoadBalancer loadBalancer) {
        configuration.setLoadBalancer(loadBalancer);
        return this;
    }

    /**
     * 代理对象配置
     *
     * @param referenceConfig 代理对象配置
     * @return 当前实例
     */
    public XrpcBootstrap reference(ReferenceConfig<?> referenceConfig) {

        REFERENCE_CONFIG_LIST.add(referenceConfig);
        referenceConfig.setRegistry(configuration.getRegistry());
        return this;
    }

    /**
     * 序列化方式配置
     *
     * @param serializeType 序列化类型
     * @return 当前实例
     */
    public XrpcBootstrap serialize(String serializeType) {

        if (StringUtils.isNotEmpty(serializeType)) {
            configuration.setSerializeType(serializeType);
        }
        return this;
    }

    /**
     * 压缩类型配置
     *
     * @param compressType 压缩类型
     * @return 当前实例
     */
    public XrpcBootstrap compress(String compressType) {

        if (StringUtils.isNotEmpty(compressType)) {
            configuration.setCompressType(compressType);
        }
        return this;
    }

    /**
     * 客户端完成配置 开启心跳检测
     */
    public void finish() {

        //获取所有需要调用的服务的主机 未去重
        List<InetSocketAddress> addressList = new ArrayList<>();
        for (ReferenceConfig<?> referenceConfig : REFERENCE_CONFIG_LIST) {
            String serviceName = referenceConfig.getInterface().getName();
            List<InetSocketAddress> addresses = configuration.getRegistry().seekServiceList(serviceName,referenceConfig.getGroup());
            addressList.addAll(addresses);
        }
        //主机 去重
        addressList = new ArrayList<>(new HashSet<>(addressList));
        ALL_SERVICE_ADDRESS_LIST = addressList;
    }








}
