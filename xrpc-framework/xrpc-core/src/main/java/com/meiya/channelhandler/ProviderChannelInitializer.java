package com.meiya.channelhandler;

import com.meiya.channelhandler.handler.RequestDecodeHandler;
import com.meiya.channelhandler.handler.RequestMethodCallHandler;
import com.meiya.channelhandler.handler.ResponseEncodeHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * @author xiaopf
 */
public class ProviderChannelInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel channel) {
        //服务提供方
        channel.pipeline()
                //入站、出站处理器1 日志
                .addLast(new LoggingHandler(LogLevel.INFO))
                //入站处理器2 请求解码
                .addLast(new RequestDecodeHandler())
                //入站处理器3 反射方法调用
                .addLast(new RequestMethodCallHandler())
                //出站处理器2 响应编码
                .addLast(new ResponseEncodeHandler());
    }
}
