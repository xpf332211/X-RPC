package com.meiya.channelhandler.handler;

import com.meiya.bootstrap.XrpcBootstrap;
import com.meiya.enumeration.ResponseCode;
import com.meiya.exceptions.ResponseException;
import com.meiya.protection.CircuitBreaker;
import com.meiya.protection.impl.StateCircuitBreaker;
import com.meiya.transport.message.RequestPayload;
import com.meiya.transport.message.XrpcRequest;
import com.meiya.transport.message.XrpcResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 入站处理器
 * @author xiaopf
 */
@Slf4j
public class ResponseCompleteHandler extends SimpleChannelInboundHandler<XrpcResponse> {
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, XrpcResponse xrpcResponse) {
        //获取熔断器 (不会为空，因为RpcConsumerInvocationHandler中会先做了缓存)
        Channel channel = channelHandlerContext.channel();
        InetSocketAddress address = (InetSocketAddress)channel.remoteAddress();
        Map<InetSocketAddress, CircuitBreaker> circuitBreakerCache = XrpcBootstrap.IP_CIRCUIT_BREAKER_CACHE;
        CircuitBreaker circuitBreaker = circuitBreakerCache.get(address);

        //对于成功的普通请求和心跳请求会记录响应成功 分别返回响应内容和空，然后在服务调用方通过get获取
        //对于失败的请求 不返回 为熔断器记录响应失败 然后直接抛出异常
        //既然不返回 会发生调用失败 会进行重试
        Object responseContext = null;
        byte responseCode = xrpcResponse.getResponseCode();
        CompletableFuture<Object> future = XrpcBootstrap.PENDING_REQUEST.get(xrpcResponse.getRequestId());
        if (responseCode == ResponseCode.SUCCESS.getCode()){
            responseContext = xrpcResponse.getResponseBody().getResponseContext();
            circuitBreaker.reportSuccess();
            log.info("id为【{}】的请求远程调用获取响应成功,即将返回调用结果给服务调用方！",xrpcResponse.getRequestId());
        }else if (responseCode == ResponseCode.SUCCESS_HEART_BEAT.getCode()){
            circuitBreaker.reportSuccess();
            log.info("id为【{}】的心跳检测获取响应成功,即将返回给服务调用方！",xrpcResponse.getRequestId());
        }else if (responseCode == ResponseCode.CURRENT_LIMIT.getCode()){
            log.warn("id为【{}】的请求远程调用被限流",xrpcResponse.getRequestId());
            circuitBreaker.reportFailure();
            throw new ResponseException(ResponseCode.CURRENT_LIMIT.getDesc());
        }else if (responseCode == ResponseCode.CLIENT_FAIL.getCode()){
            log.warn("id为【{}】的请求远程调用找不到目标资源",xrpcResponse.getRequestId());
            circuitBreaker.reportFailure();
            throw new ResponseException(ResponseCode.CLIENT_FAIL.getDesc());
        }else if (responseCode == ResponseCode.SERVER_ERROR.getCode()){
            log.warn("id为【{}】的请求远程调用的服务器内部错误",xrpcResponse.getRequestId());
            circuitBreaker.reportFailure();
            throw new ResponseException(ResponseCode.SERVER_ERROR.getDesc());
        }else if (responseCode == ResponseCode.SERVER_CLOSING.getCode()){
            log.warn("id为【{}】的请求远程调用的服务器正在关闭",xrpcResponse.getRequestId());
            //修正服务列表
            XrpcBootstrap.ALL_SERVICE_ADDRESS_LIST.remove(address);
            XrpcBootstrap.CHANNEL_CACHE.remove(address);
            //修正负载均衡器
            XrpcRequest request = XrpcBootstrap.REQUEST_THREAD_LOCAL.get();
            RequestPayload payload = request.getRequestPayload();
            String serviceName = null;
            if (payload != null){
               serviceName = payload.getInterfaceName();
            }
            XrpcBootstrap.getInstance().getConfiguration().getLoadBalancer().reLoadBalance(serviceName,XrpcBootstrap.ALL_SERVICE_ADDRESS_LIST);
            throw new ResponseException(ResponseCode.SERVER_CLOSING.getDesc());
        }

        future.complete(responseContext);
    }
}
