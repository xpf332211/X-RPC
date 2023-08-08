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
    public static void detect() {
        List<InetSocketAddress> addressList = XrpcBootstrap.ALL_SERVICE_ADDRESS_LIST;
        //1.与需要调用的服务的所有主机地址作连接 并缓存
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
                    log.error("心跳检测前，获取【{}】的channel时发生异常", address);
                    throw new DiscoveryException(e);
                }
                //缓存channel
                XrpcBootstrap.CHANNEL_CACHE.put(address, channel);
            }
            if (channel == null) {
                log.error("心跳检测前，获取或建立与【{}】服务提供方的channel时发生异常", address);
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
            new Timer().schedule(new ResponseTimerTask(), 0, 2 * 1000);
        });
    }

    private static class ResponseTimerTask extends TimerTask {
        @SneakyThrows
        @Override
        public void run() {
            log.info("一次心跳检测开始");
            XrpcBootstrap.RESPONSE_TIME_CHANNEL_CACHE.clear();
            Map<InetSocketAddress, Channel> channelCache = XrpcBootstrap.CHANNEL_CACHE;
            for (Map.Entry<InetSocketAddress, Channel> entry : channelCache.entrySet()) {
                InetSocketAddress address = entry.getKey();
                Channel channel = entry.getValue();
                //3.1 一次请求的发送 开始计时
                long start = System.currentTimeMillis();
                //重试机制
                int maxRetries = 3;
                int retryCount = 0;
                boolean success = false;
                while (retryCount < maxRetries && !success) {
                    XrpcRequest request = buildHeartbeatRequest();
                    CompletableFuture<Object> completableFuture = new CompletableFuture<>();
                    //对外暴露这个completableFuture
                    XrpcBootstrap.PENDING_REQUEST.put(request.getRequestId(), completableFuture);
                    channel.writeAndFlush(request);
                    log.info("服务调用方,向【{}】主机发送了id为【{}】的心跳请求", entry.getKey(), request.getRequestId());
                    try {
                        completableFuture.get(1, TimeUnit.SECONDS);
                        success = true;
                    } catch (InterruptedException | ExecutionException | TimeoutException e) {
                        retryCount++;
                        log.error("心跳检测时，获取和【{}】的主机连接异常,正在进行第【{}】次重试", address, retryCount);
                        //睡眠随机数 防止重试风暴
                        Thread.sleep(10 + new Random().nextInt(20));
                    }
                }
                if (success) {
                    if (retryCount != 0) {
                        log.info("第【{}】次重试与【{}】主机的连接成功", retryCount, address);
                    }
                    //3.2 一次响应的接收 结束计时
                    long end = System.currentTimeMillis();
                    long rtt = end - start;
                    log.info("服务调用方心跳检测可用主机【{}】的响应时长为【{}】ms", entry.getKey(), rtt);
                    //3.3记录到treeMap中，方便取得最短响应主机
                    TreeMap<Long, List<Channel>> cache = XrpcBootstrap.RESPONSE_TIME_CHANNEL_CACHE;
                    if (cache.containsKey(rtt)) {
                        cache.get(rtt).add(channel);
                    } else {
                        List<Channel> channels = new ArrayList<>();
                        channels.add(channel);
                        cache.put(rtt, channels);
                    }
                } else {
                    log.info("【{}】次重试均无法获取到和【{}】主机的连接,不再向其发送心跳请求", maxRetries, address);
                    //服务异常 从所有服务主机列表、channel缓存中删除该服务
                    XrpcBootstrap.ALL_SERVICE_ADDRESS_LIST.remove(address);
                    XrpcBootstrap.CHANNEL_CACHE.remove(address);
                }
            }
            for (Map.Entry<Long, List<Channel>> entry : XrpcBootstrap.RESPONSE_TIME_CHANNEL_CACHE.entrySet()) {
                Long time = entry.getKey();
                List<Channel> channels = entry.getValue();
                for (Channel channel : channels) {
                    log.info("【{}】--->【{}】", channel.remoteAddress(), time);
                }
            }
            log.info("一次心跳检测结束");


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
    }
}
