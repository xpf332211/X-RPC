package com.meiya.netty.demo2;


import com.meiya.exceptions.NettyException;
import com.meiya.utils.print.Out;
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

    public void run() {
        Bootstrap bootstrap = new Bootstrap();
        NioEventLoopGroup group = new NioEventLoopGroup();

        try{
            ChannelFuture channelFuture = bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new ChannelInitializer<>() {
                        @Override
                        protected void initChannel(Channel channel) {
                            channel.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                                @Override
                                protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
                                    Out.println("客户端接收到消息：" + byteBuf.toString(StandardCharsets.UTF_8));
                                }
                            });
                        }
                    })
                    .connect(host, port)
                    .sync();    //阻塞等待连接建立
            Channel channel = channelFuture.channel();
            String line = "哎呀";
            channel.writeAndFlush(Unpooled.copiedBuffer(line, StandardCharsets.UTF_8));
            channel.closeFuture().sync();
        }catch (InterruptedException e){
            log.info("客户端netty启动时发生异常");
            throw new NettyException(e);
        }finally {
            group.shutdownGracefully();
        }


    }

    public static void main(String[] args) throws InterruptedException {
        new Client("127.0.0.1", 8081).run();
    }
}
