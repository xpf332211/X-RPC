package com.meiya.channelhandler.handler;

import com.meiya.bootstrap.XrpcBootstrap;
import com.meiya.enumeration.ResponseCode;
import com.meiya.transport.message.XrpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.CompletableFuture;

/**
 * 入站处理器
 * @author xiaopf
 */
@Slf4j
public class ResponseCompleteHandler extends SimpleChannelInboundHandler<XrpcResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, XrpcResponse xrpcResponse) throws Exception {
        Object responseContext = null;
        CompletableFuture<Object> future = XrpcBootstrap.PENDING_REQUEST.get(xrpcResponse.getRequestId());
        if (xrpcResponse.getResponseCode() == ResponseCode.SUCCESS.getCode()){
            responseContext = xrpcResponse.getResponseBody().getResponseContext();
            log.info("id为【{}】的响应即将返回调用结果！",xrpcResponse.getRequestId());
        }

        future.complete(responseContext);
    }
}
