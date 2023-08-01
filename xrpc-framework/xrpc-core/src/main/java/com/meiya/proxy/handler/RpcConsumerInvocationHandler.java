package com.meiya.proxy.handler;

import com.meiya.NettyBootstrap;
import com.meiya.XrpcBootstrap;
import com.meiya.enumeration.RequestType;
import com.meiya.exceptions.DiscoveryException;
import com.meiya.exceptions.NettyException;
import com.meiya.registry.Registry;
import com.meiya.transport.message.RequestPayload;
import com.meiya.transport.message.XrpcRequest;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * 核心类
 * 封装了客户端通信的基础逻辑
 * 1.发现可用服务
 * 2.建立连接
 * 3.发送请求
 * 4.得到结果
 *
 * @author xiaopf
 */
@Slf4j
public class RpcConsumerInvocationHandler implements InvocationHandler {

    private final Registry registry;
    private final Class<?> interfaceRef;

    public RpcConsumerInvocationHandler(Registry registry, Class<?> interfaceRef) {
        this.registry = registry;
        this.interfaceRef = interfaceRef;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //1.服务发现 从注册中心寻找一个可用服务
        InetSocketAddress address = registry.seek(interfaceRef.getName());
        log.info("服务调用方,获取了服务【{}】的可用主机【{}】", interfaceRef.getName(), address);

        //2.服务调用方启动netty 连接服务提供方 发送需要调用的服务的信息
        Channel channel = getAvailableChannel(address);
        log.info("服务调用方,获取了和【{}】主机建立的连接通道,准备发送请求", address);
        //3.封装请求
        RequestPayload payload = RequestPayload.builder()
                .interfaceName(interfaceRef.getName())
                .methodName(method.getName())
                .parametersType(method.getParameterTypes())
                .parametersValue(args)
                .returnType(method.getReturnType())
                .build();
        long requestId = XrpcBootstrap.ID_GENERATOR.getId();
        XrpcRequest xrpcRequest = XrpcRequest.builder()
                .requestId(requestId)
                .compressType((byte) 1)
                .serializeType((byte) 1)
                .requestType(RequestType.REQUEST.getId())
                .requestPayload(payload)
                .build();
        //4.写出请求
        CompletableFuture<Object> completableFuture = new CompletableFuture<>();
        //对外暴露这个completableFuture
        XrpcBootstrap.PENDING_REQUEST.put(1L, completableFuture);
        channel.writeAndFlush(xrpcRequest)
                .addListener((ChannelFutureListener) future -> {
                    log.info("服务调用方发送了id为【{}】的请求：【{}】",requestId, xrpcRequest.toString());
                });


        //5.获取响应的结果
        //阻塞等待其他地方处理这个completableFuture
        Object result = completableFuture.get(10, TimeUnit.SECONDS);
        log.info("id为【{}】的请求得到调用结果为【{}】",requestId,result);
        return result;
    }


    /**
     * 获取一个可用的channel
     *
     * @param address 与服务提供方的连接地址
     * @return 可用的channel
     */
    private Channel getAvailableChannel(InetSocketAddress address) {
        //从缓存中获取channel
        Channel channel = XrpcBootstrap.CHANNEL_CACHE.get(address);
        //若获取不到，则新建并加入缓存
        if (channel == null) {
            //建立一个新的channel
            Bootstrap bootstrap = NettyBootstrap.getBootstrap();
            CompletableFuture<Channel> channelCompletableFuture = new CompletableFuture<>();
            bootstrap.connect(address)
                    .addListener((ChannelFutureListener) future -> {
                        if (future.isDone()) {
                            log.info("服务调用方已经和【{}】服务提供方连接成功", address);
                            //如果连接建立完毕 将连接存在channelCompletableFuture中，便于主线程获取
                            channelCompletableFuture.complete(future.channel());
                        }
                        if (!future.isSuccess()) {
                            //异常处理
                            channelCompletableFuture.completeExceptionally(future.cause());
                        }
                    });
            try {
                channel = channelCompletableFuture.get(10, TimeUnit.SECONDS);
            } catch (InterruptedException | ExecutionException | TimeoutException e) {
                log.error("获取channel时发生异常", e);
                throw new DiscoveryException(e);
            }
            //缓存channel
            XrpcBootstrap.CHANNEL_CACHE.put(address, channel);
        }
        if (channel == null) {
            log.error("获取或建立与【{}】服务提供方的channel时发生异常", address);
            throw new NettyException("获取或建立channel时发生异常");
        }
        return channel;
    }
}