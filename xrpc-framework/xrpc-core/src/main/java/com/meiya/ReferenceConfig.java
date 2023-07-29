package com.meiya;


import com.meiya.exceptions.NettyException;
import com.meiya.registry.Registry;
import com.meiya.utils.print.Out;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author xiaopf
 */
@Slf4j
public class ReferenceConfig<T> {
    private Class<T> interfaceRef;
    private Registry registry;


    public Class<T> getInterface() {
        return interfaceRef;
    }

    public void setInterface(Class<T> interfaceRef) {
        this.interfaceRef = interfaceRef;
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    /**
     * 代理设计模式 生成一个api接口的代理对象
     *
     * @return 代理对象
     */
    public T get() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class[] classes = new Class[]{interfaceRef};
        //使用动态代理 生成代理对象
        Object proxy = Proxy.newProxyInstance(classLoader, classes, (proxy1, method, args) -> {
            //1.服务发现 从注册中心寻找一个可用服务
            InetSocketAddress address = registry.seek(interfaceRef.getName());
            log.info("服务调用方,返回了服务【{}】的可用主机【{}】", interfaceRef.getName(), address);

            //2.服务调用方启动netty 连接服务提供方 发送需要调用的服务的信息

            //从缓存中获取channel
            Channel channel = XrpcBootstrap.CHANNEL_CACHE.get(address);
            //若获取不到，则新建并加入缓存
            if (channel == null) {
                //建立一个新的channel
                Bootstrap bootstrap = NettyBootstrap.getBootstrap();
                CompletableFuture<Channel> channelCompletableFuture = new CompletableFuture<>();
                bootstrap.connect(address)
                        .addListener((ChannelFutureListener)future -> {
                            if (future.isDone()){
                                log.info("已经和【{}】服务提供方连接成功",address);
                                //如果连接建立完毕 将连接存在channelCompletableFuture中，便于主线程获取
                                channelCompletableFuture.complete(future.channel());
                            }
                            if (!future.isSuccess()){
                                //异常处理
                                channelCompletableFuture.completeExceptionally(future.cause());
                            }
                        });
                channel = channelCompletableFuture.get(10, TimeUnit.SECONDS);
                //缓存channel
                XrpcBootstrap.CHANNEL_CACHE.put(address, channel);
            }
            if (channel == null) {
                log.error("获取或建立与【{}】服务提供方的channel时发生异常",address);
                throw new NettyException("获取或建立channel时发生异常");
            }
            CompletableFuture<Object> completableFuture = new CompletableFuture<>();
            //对外暴露这个completableFuture
            XrpcBootstrap.PENDING_REQUEST.put(1L,completableFuture);
            String line = "hello,来自服务调用方的消息~~";
            channel.writeAndFlush(Unpooled.copiedBuffer(line, StandardCharsets.UTF_8))
                    .addListener((ChannelFutureListener)future -> {

                    });
            log.info("服务调用方发送了消息：【{}】",line);
            //阻塞等待其他地方处理这个completableFuture
            Object o = completableFuture.get(10, TimeUnit.SECONDS);
            return o;
        });
        return (T) proxy;
    }


}
