package com.meiya;

import com.meiya.channelHandler.ConsumerChannelInitializer;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

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
                .handler(new ConsumerChannelInitializer());
    }
    private NettyBootstrap(){}
    public static Bootstrap getBootstrap(){
        return bootstrap;
    }

}
