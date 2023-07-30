package com.meiya.channelHandler;

import com.meiya.channelHandler.handler.RequestEncodeHandler;
import com.meiya.channelHandler.handler.ResponseCompleteHandler;
import com.meiya.channelHandler.handler.ResponseDecodeHandler;
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
        //服务调用方
        channel.pipeline()
                //出站、入站处理器1 日志
                .addLast(new LoggingHandler(LogLevel.INFO))
                //入站处理器2 解析响应报文
                .addLast(new ResponseDecodeHandler())
                //入站处理器3 处理结果
                .addLast(new ResponseCompleteHandler())
                //出站处理器2 封装请求报文
                .addLast(new RequestEncodeHandler());

    }
}
