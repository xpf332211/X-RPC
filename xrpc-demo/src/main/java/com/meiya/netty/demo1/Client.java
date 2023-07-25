package com.meiya.netty.demo1;


import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author xiaopf
 */
@Slf4j
public class Client {
    String host;
    int port;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void run() throws InterruptedException {
        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup group = new NioEventLoopGroup();

        ChannelFuture channelFuture = bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(Channel channel) {
                        channel.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
                                System.out.println("客户端接收到消息：" + byteBuf.toString(StandardCharsets.UTF_8));
                            }
                        });
                    }
                })
                .connect(host, port)
                .sync();    //阻塞等待连接建立
        Channel channel = channelFuture.channel();

        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                1,
                4,
                10,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(10),
                Executors.defaultThreadFactory(),
                new ThreadPoolExecutor.AbortPolicy()
        );
        executor.execute(() -> {
            Scanner scanner = new Scanner(System.in);
            while (true) {
                String line = scanner.nextLine();
                if ("q".equals(line)) {
                    channel.close();
                    break;
                } else {
                    channel.writeAndFlush(Unpooled.copiedBuffer(line, StandardCharsets.UTF_8));
                }
            }
        });
        //监听close,优雅停止
        channel.closeFuture().addListener(future -> {
            executor.shutdown();
            group.shutdownGracefully();
        });
    }

    public static void main(String[] args) throws InterruptedException {
        new Client("127.0.0.1", 8080).run();
    }
}
