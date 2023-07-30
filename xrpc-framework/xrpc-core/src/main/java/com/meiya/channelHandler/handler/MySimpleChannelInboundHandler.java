package com.meiya.channelHandler.handler;

import com.meiya.XrpcBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.nio.charset.Charset;
import java.util.concurrent.CompletableFuture;

/**
 * @author xiaopf
 */
public class MySimpleChannelInboundHandler extends SimpleChannelInboundHandler<ByteBuf> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf buf) throws Exception {
        CompletableFuture<Object> future = XrpcBootstrap.PENDING_REQUEST.get(1L);
        String result = "服务调用方已经收到服务提供方的消息：" + "【" + buf.toString(Charset.defaultCharset()) + "】";
        future.complete(result);
    }
}
