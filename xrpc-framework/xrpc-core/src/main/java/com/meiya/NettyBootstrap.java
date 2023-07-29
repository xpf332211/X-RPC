package com.meiya;

import com.meiya.utils.print.Out;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;

/**
 * @author xiaopf
 */
public class NettyBootstrap {
    private static Bootstrap bootstrap = new Bootstrap();
    private static final NioEventLoopGroup GROUP = new NioEventLoopGroup();
    //考虑线程安全问题，将bootstrap的初始化操作放在静态代码块中完成。类加载时便初始化完毕，无需等待调用方法时初始化。
    static {
        bootstrap = bootstrap.group(GROUP)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<>() {
                    @Override
                    protected void initChannel(Channel channel) {
                        channel.pipeline().addLast(new SimpleChannelInboundHandler<ByteBuf>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf buf) throws Exception {
                                CompletableFuture<Object> future = XrpcBootstrap.PENDING_REQUEST.get(1L);
                                String result = "服务调用方已经收到服务提供方的消息：" + "【" + buf.toString(Charset.defaultCharset()) + "】";
                                future.complete(result);
                            }
                        });
                    }
                });
    }
    private NettyBootstrap(){}
    public static Bootstrap getBootstrap(){
        return bootstrap;
    }

}
