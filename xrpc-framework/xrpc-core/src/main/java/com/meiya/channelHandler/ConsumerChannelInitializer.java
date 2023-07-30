package com.meiya.channelHandler;

import com.meiya.channelHandler.handler.MessageEncoderHandler;
import com.meiya.channelHandler.handler.MySimpleChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * @author xiaopf
 */
public class ConsumerChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        //服务提供方
        channel.pipeline()
                //出站、入站处理器1 日志
                .addLast(new LoggingHandler(LogLevel.INFO))
                //入站处理器2 接收服务提供方返回的结果 暂时不用
                .addLast(new MySimpleChannelInboundHandler())
                //出站处理器2 封装报文
                .addLast(new MessageEncoderHandler());

    }
}
