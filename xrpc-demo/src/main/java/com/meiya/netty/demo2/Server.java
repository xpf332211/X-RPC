package com.meiya.netty.demo2;

import com.meiya.exceptions.NettyException;
import com.meiya.utils.print.Out;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import lombok.extern.slf4j.Slf4j;

import java.nio.charset.StandardCharsets;

/**
 * @author xiaopf
 */
@Slf4j
public class Server {
    private final int port;

    public Server(int port) {
        this.port = port;
    }
    public void start(){
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        NioEventLoopGroup boss = new NioEventLoopGroup(2);
        NioEventLoopGroup worker = new NioEventLoopGroup(10);

        try {
            ChannelFuture channelFuture = serverBootstrap.group(boss, worker)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel channel) {
                            channel.pipeline().addLast(new StringDecoder(StandardCharsets.UTF_8));
                            channel.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelRead(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
                                    String msg = o.toString();
                                    Out.println("服务端收到消息：" + msg);
                                    channelHandlerContext.writeAndFlush(Unpooled.copiedBuffer("我是服务端，我已经收到您的消息为:" + msg, StandardCharsets.UTF_8));
                                }
                            });
                        }
                    })
                    .bind(port)
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

    public static void main(String[] args) throws InterruptedException {
        new Server(8081).start();

    }
}
