package com.meiya.detection;

import com.meiya.NettyBootstrap;
import com.meiya.XrpcBootstrap;
import com.meiya.compress.CompressorFactory;
import com.meiya.enumeration.RequestType;
import com.meiya.exceptions.DiscoveryException;
import com.meiya.exceptions.NettyException;
import com.meiya.registry.Registry;
import com.meiya.serialize.SerializerFactory;
import com.meiya.transport.message.XrpcRequest;
import com.meiya.utils.print.Out;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author xiaopf
 */
@Slf4j
public class HeartbeatDetector {
    public static void detect(String serviceName) {
        //1.根据服务名获取对应的可用主机地址
        Registry registry = XrpcBootstrap.getInstance().getRegistry();
        List<InetSocketAddress> addressList = registry.seekServiceList(serviceName);
        //2.与这些主机地址作连接 并缓存
        for (InetSocketAddress address : addressList) {
            Channel channel = XrpcBootstrap.CHANNEL_CACHE.get(address);
            if (channel == null) {
                Bootstrap bootstrap = NettyBootstrap.getBootstrap();
                CompletableFuture<Channel> channelCompletableFuture = new CompletableFuture<>();
                bootstrap.connect(address)
                        .addListener((ChannelFutureListener) future -> {
                            if (future.isDone()) {
                                log.info("心跳检测前，服务调用方已经和【{}】服务提供方连接成功", address);
                                //如果连接建立完毕 将连接存在channelCompletableFuture中，便于主线程获取
                                channelCompletableFuture.complete(future.channel());
                            }
                            if (!future.isSuccess()) {
                                //异常处理
                                channelCompletableFuture.completeExceptionally(future.cause());
                            }
                        });
                try {
                    channel = channelCompletableFuture.get();
                } catch (InterruptedException | ExecutionException e) {
                    log.error("心跳检测【{}】服务前，获取【{}】的channel时发生异常", serviceName, address);
                    throw new DiscoveryException(e);
                }
                //缓存channel
                XrpcBootstrap.CHANNEL_CACHE.put(address, channel);
            }
            if (channel == null) {
                log.error("心跳检测【{}】服务前，获取或建立与【{}】服务提供方的channel时发生异常", serviceName, address);
            }
        }
        //3.线程池 + 定时任务
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                1,
                1,
                10,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(10),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy()
        );
        executor.execute(() -> {
            new Timer().schedule(new TimerTask() {
                @SneakyThrows
                @Override
                public void run() {
                    Map<InetSocketAddress, Channel> channelCache = XrpcBootstrap.CHANNEL_CACHE;
                    for (Map.Entry<InetSocketAddress, Channel> entry : channelCache.entrySet()) {
                        Channel channel = entry.getValue();
                        //3.1 一次请求的发送 开始计时
                        long start = System.currentTimeMillis();
                        XrpcRequest request = buildHeartbeatRequest();
                        CompletableFuture<Object> completableFuture = new CompletableFuture<>();
                        //对外暴露这个completableFuture
                        XrpcBootstrap.PENDING_REQUEST.put(request.getRequestId(), completableFuture);
                        channel.writeAndFlush(request);
                        log.info("服务调用方向【{}】服务的【{}】主机发送了id为【{}】的心跳请求", serviceName, entry.getKey(),request.getRequestId());
                        completableFuture.get();
                        //3.2 一次响应的接收 结束计时
                        long end = System.currentTimeMillis();
                        long rtt = end - start;
                        XrpcBootstrap.SERVICE_RESPONSE_TIME_CACHE.put(serviceName,new TreeMap<>(){{
                            put(rtt,channel);
                        }});
                        log.info("服务调用方心跳检测【{}】服务的可用主机【{}】的响应时长为【{}】ms", serviceName, entry.getKey(), rtt);

                    }

                    Out.println("=====================================================");
                    for (Map.Entry<String, TreeMap<Long, Channel>> entry1 : XrpcBootstrap.SERVICE_RESPONSE_TIME_CACHE.entrySet()){
                        log.info("【{}】服务的主机响应时间如下",entry1.getKey());
                        for (Map.Entry<Long, Channel> entry2 : entry1.getValue().entrySet()){
                            Long time = entry2.getKey();
                            String channel1 = entry2.getValue().toString();
                            log.info("【{}】 --> 【{}】",time,channel1);
                        }
                    }
                }


                private XrpcRequest buildHeartbeatRequest() {
                    long requestId = XrpcBootstrap.ID_GENERATOR.getId();
                    byte serializerCode = SerializerFactory.getSerializerCode(XrpcBootstrap.SERIALIZE_TYPE);
                    byte compressorCode = CompressorFactory.getCompressorCode(XrpcBootstrap.COMPRESSOR_TYPE);
                    return XrpcRequest.builder()
                            .requestId(requestId)
                            .compressType(compressorCode)
                            .serializeType(serializerCode)
                            .requestType(RequestType.HEART_BEAT.getId())
                            .build();
                }
            }, 0, 2 * 1000);
        });
    }
}
