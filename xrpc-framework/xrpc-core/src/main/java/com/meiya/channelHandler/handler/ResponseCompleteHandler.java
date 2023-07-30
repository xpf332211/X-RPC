package com.meiya.channelHandler.handler;

import com.meiya.XrpcBootstrap;
import com.meiya.transport.message.XrpcResponse;
import com.meiya.utils.print.Out;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.util.concurrent.CompletableFuture;

/**
 * 入站处理器
 * @author xiaopf
 */
public class ResponseCompleteHandler extends SimpleChannelInboundHandler<XrpcResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, XrpcResponse xrpcResponse) throws Exception {
        CompletableFuture<Object> future = XrpcBootstrap.PENDING_REQUEST.get(1L);
        Object responseContext = xrpcResponse.getResponseBody().getResponseContext();
        future.complete(responseContext);
    }
}
