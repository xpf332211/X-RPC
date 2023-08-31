package com.meiya.channelhandler.handler;

import com.meiya.config.ServiceConfig;
import com.meiya.bootstrap.XrpcBootstrap;
import com.meiya.enumeration.RequestType;
import com.meiya.enumeration.ResponseCode;
import com.meiya.hook.ShutDownHolder;
import com.meiya.hook.XrpcShutDownHook;
import com.meiya.protection.CurrentLimiter;
import com.meiya.protection.impl.TokenBucketCurrentLimiter;
import com.meiya.transport.message.RequestPayload;
import com.meiya.transport.message.ResponseBody;
import com.meiya.transport.message.XrpcRequest;
import com.meiya.transport.message.XrpcResponse;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.IllegalFormatCodePointException;

/**
 * @author xiaopf
 */
@Slf4j
public class RequestMethodCallHandler extends SimpleChannelInboundHandler<XrpcRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, XrpcRequest xrpcRequest) throws Exception {

        //1.封装部分响应
        long requestId = xrpcRequest.getRequestId();
        byte serializerCode = xrpcRequest.getSerializeType();
        byte compressCode = xrpcRequest.getCompressType();
        ResponseBody responseBody = null;
        byte responseCode = -1;
        XrpcResponse xrpcResponse = XrpcResponse.builder()
                .serializeType(serializerCode)
                .compressType(compressCode)
                .requestId(requestId)
                .build();

        //2.查看挡板是否打开 若打开则设置状态码 然后直接返回
        if (ShutDownHolder.BAFFLE.get()){
            xrpcResponse.setResponseCode(ResponseCode.SERVER_CLOSING.getCode());
            channelHandlerContext.channel().writeAndFlush(xrpcResponse);
            return;
        }
        //3.请求计数器 加一
        ShutDownHolder.REQUEST_COUNTER.increment();

        //4.获取ip限流器
        Channel channel = channelHandlerContext.channel();
        InetSocketAddress address = (InetSocketAddress) channel.remoteAddress();
        CurrentLimiter currentLimiter = XrpcBootstrap.IP_CURRENT_LIMITER_CACHE.get(address);
        if (currentLimiter == null){
             currentLimiter = new TokenBucketCurrentLimiter(500, 500);
            XrpcBootstrap.IP_CURRENT_LIMITER_CACHE.put(address,currentLimiter);
        }



        //5.判断限流情况
        boolean allowRequest = currentLimiter.allowRequestPass();
        //5.1 被限流 无需调用方法 直接返回限流响应 注意心跳请求不应该被限流，也就不会被熔断了
        if (!allowRequest && xrpcRequest.getRequestType() != RequestType.HEART_BEAT.getId()){
            responseCode = ResponseCode.CURRENT_LIMIT.getCode();
        }
        //5.2 心跳请求
        else if (xrpcRequest.getRequestType() == RequestType.HEART_BEAT.getId()){
            responseCode = ResponseCode.SUCCESS_HEART_BEAT.getCode();
        }
        //5.3 普通请求
        else if (xrpcRequest.getRequestType() == RequestType.REQUEST.getId()){
            //获取请求体
            RequestPayload requestPayload = xrpcRequest.getRequestPayload();
            //方法调用
            Object result = null;
            try{
                //反射调用
                result = callTargetMethod(requestPayload);
                //处理成功状态码
                responseCode = ResponseCode.SUCCESS.getCode();
                //封装响应
                responseBody = ResponseBody.builder()
                        .responseContext(result)
                        .build();
            }catch (Exception e){
                //处理失败状态码
                responseCode = ResponseCode.SERVER_ERROR.getCode();
            }

        }
        //6.填充响应
        xrpcResponse.setResponseCode(responseCode);
        xrpcResponse.setResponseBody(responseBody);




        //7.写出响应
        channel.writeAndFlush(xrpcResponse).addListener(future -> {
            if(log.isDebugEnabled()){
                log.debug("服务提供方发送了id为【{}】的响应", requestId);
            }
        });

        //8.请求计数器 减一 并判断请求数是否为0了 若是则通知XrpcShutDownHook结束阻塞
        ShutDownHolder.REQUEST_COUNTER.decrement();
        if (ShutDownHolder.REQUEST_COUNTER.sum() == 0){
            synchronized (XrpcShutDownHook.SHUT_DOWN_HOOK_LOCK){
                notifyAll();
            }
        }
    }

    /**
     * 反射调用
     * @param requestPayload 请求负载
     * @return 调用返回值
     */
    private Object callTargetMethod(RequestPayload requestPayload) {
        String interfaceName = requestPayload.getInterfaceName();
        String methodName = requestPayload.getMethodName();
        Class<?>[] parametersType = requestPayload.getParametersType();
        Object[] parametersValue = requestPayload.getParametersValue();
        Class<?> returnType = requestPayload.getReturnType();
        //通过接口名获取到对应的实现类 spring中可以用getBean,传入一个接口返回一个实现类
        //此处我们自定义了一个全局的服务列表
        ServiceConfig<?> serviceConfig = XrpcBootstrap.SERVICE_MAP.get(interfaceName);
        Object refImpl = serviceConfig.getRef();
        //反射调用
        Object returnValue = null;
        try {
            Method method = refImpl.getClass().getMethod(methodName, parametersType);
            returnValue = method.invoke(refImpl, parametersValue);
        } catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e) {
            log.error("反射服务【{}】调用【{}】方法时发生异常", interfaceName, methodName);
            throw new RuntimeException(e);
        }
        return returnValue;
    }
}
