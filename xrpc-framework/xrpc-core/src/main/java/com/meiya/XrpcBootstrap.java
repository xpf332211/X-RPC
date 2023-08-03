package com.meiya;

import com.meiya.channelhandler.ProviderChannelInitializer;
import com.meiya.exceptions.NettyException;
import com.meiya.loadbalancer.LoadBalancer;
import com.meiya.loadbalancer.impl.ConsistentHashLoadBalancer;
import com.meiya.loadbalancer.impl.RoundRobinLoadBalancer;
import com.meiya.registry.Registry;
import com.meiya.transport.message.XrpcRequest;
import com.meiya.utils.IdGenerator;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;


import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author xiaopf
 */
@Slf4j
public class XrpcBootstrap {


    private String applicationName;
    private RegistryConfig registryConfig;
    private ProtocolConfig protocolConfig;


    /**
     * threadLocal
     */
    public static final ThreadLocal<XrpcRequest> REQUEST_THREAD_LOCAL = new ThreadLocal<>();
    /**
     * 负载均衡器
     */
    public static LoadBalancer LOAD_BALANCER;
    /**
     * 端口
     */
    public static int PORT = 8081;
    /**
     * 注册中心
     */
    private Registry registry;
    public Registry getRegistry(){
        return this.registry;
    }

    /**
     * 序列化类型 默认为jdk
     */
    public static String SERIALIZE_TYPE = "jdk";

    /**
     * 压缩类型 默认为gzip
     */
    public static String COMPRESSOR_TYPE = "gzip";

    /**
     * id生成器
     */
    public static final IdGenerator ID_GENERATOR = new IdGenerator(2,10);

    /**
     * 全局对外挂起的completableFuture
     * key为请求的唯一标识
     */
    public static final Map<Long, CompletableFuture<Object>> PENDING_REQUEST = new ConcurrentHashMap<>(128);


    /**
     * 连接缓存
     * 使用类作为key 要注意该类是否重写了hashcode和equals方法
     */
    public static final Map<InetSocketAddress,Channel> CHANNEL_CACHE = new ConcurrentHashMap<>(16);
    /**
     * 服务列表
     * key--> interface的全限名 value--> ServiceConfig
     */
    public static final Map<String,ServiceConfig<?>> SERVICE_MAP = new ConcurrentHashMap<>(16);
    /**
     * XrpcBootstrap是单例 采用饿汉式方法创建
     */
    private static final XrpcBootstrap XRPC_BOOTSTRAP = new XrpcBootstrap();

    private XrpcBootstrap(){
        //初始化
    }

    public static XrpcBootstrap getInstance(){
        return XRPC_BOOTSTRAP;
    }


    /**
     * 定义当前应用的名称
     * @param applicationName   应用名称
     * @return  当前实例
     */
    public XrpcBootstrap application(String applicationName) {
        this.applicationName = applicationName;
        return this;
    }

    /**
     * 配置注册中心
     * @param registryConfig 注册中心的封装
     * @return 当前实例
     */
    public XrpcBootstrap registry(RegistryConfig registryConfig) {
        this.registry = registryConfig.getRegistry();
        LOAD_BALANCER = new ConsistentHashLoadBalancer();
        return this;
    }

    /**
     * 配置协议
     * @param protocolConfig 协议的封装
     * @return 当前实例
     */
    public XrpcBootstrap protocol(ProtocolConfig protocolConfig) {
        this.protocolConfig = protocolConfig;
        return this;
    }

    /**
     * 服务提供方 启动netty服务
     */
    public void start() {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        NioEventLoopGroup boss = new NioEventLoopGroup(2);
        NioEventLoopGroup worker = new NioEventLoopGroup(10);

        try {
            ChannelFuture channelFuture = serverBootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ProviderChannelInitializer())
                    .bind(PORT)
                    .sync();
            Channel channel = channelFuture.channel();
            channel.closeFuture().sync();
        }catch (InterruptedException e){
            log.info("服务端netty启动时发生异常!");
            throw new NettyException(e);
        }finally {
            try {
                boss.shutdownGracefully().sync();
                worker.shutdownGracefully().sync();
            }catch (Exception e){
                log.info("服务端netty优雅关闭时发生异常！");
                throw new NettyException(e);
            }
        }
    }


    //----------------------------------provider相关的api--------------------------------------

    /**
     * 服务发布
     * @param serviceConfig 需要发布的服务的封装
     * @return 当前实例
     */
    public XrpcBootstrap publish(ServiceConfig<?> serviceConfig) {
        //将服务注册到注册中心上
        registry.register(serviceConfig);
        //维护服务列表
        SERVICE_MAP.put(serviceConfig.getInterface().getName(),serviceConfig);
        return this;
    }

    /**
     * 批量 服务发布
     * @param serviceConfigList 需要发布的服务的封装的集合
     * @return 当前实例
     */
    public XrpcBootstrap publish(List<ServiceConfig<?>> serviceConfigList){
        serviceConfigList.forEach(serviceConfig -> {
            registry.register(serviceConfig);
            SERVICE_MAP.put(serviceConfig.getInterface().getName(),serviceConfig);
        });
        return this;
    }


    //----------------------------------consumer相关的api--------------------------------------


    /**
     * 代理对象配置
     * @param referenceConfig 代理对象配置
     * @return 当前实例
     */
    public XrpcBootstrap reference(ReferenceConfig<?> referenceConfig) {
        referenceConfig.setRegistry(registry);
        return this;
    }

    /**
     * 序列化方式配置
     * @param serializeType 序列化类型
     * @return 当前实例
     */
    public XrpcBootstrap serialize(String serializeType) {

        if (StringUtils.isNotEmpty(serializeType)){
            SERIALIZE_TYPE = serializeType;
        }
        return this;
    }

    /**
     * 压缩类型配置
     * @param compressType 压缩类型
     * @return 当前实例
     */
    public XrpcBootstrap compress(String compressType) {

        if (StringUtils.isNotEmpty(compressType)){
            COMPRESSOR_TYPE = compressType;
        }
        return this;
    }
}
