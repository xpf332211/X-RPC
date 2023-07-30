package com.meiya.channelHandler.handler;

import com.meiya.ServiceConfig;
import com.meiya.XrpcBootstrap;
import com.meiya.transport.message.RequestPayload;
import com.meiya.transport.message.XrpcRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author xiaopf
 */
@Slf4j
public class MethodCallHandler extends SimpleChannelInboundHandler<XrpcRequest> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, XrpcRequest xrpcRequest) throws Exception {
        //获取请求体
        RequestPayload requestPayload = xrpcRequest.getRequestPayload();
        //方法调用
        Object o = callTargetMethod(requestPayload);
        //封装响应

        //写出响应
    }

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
        try{
            Method method = refImpl.getClass().getMethod(methodName,parametersType);
            returnValue = method.invoke(refImpl, parametersValue);
        }catch (InvocationTargetException | NoSuchMethodException | IllegalAccessException e){
            log.error("反射服务【{}】调用【{}】方法时发生异常",interfaceName,methodName);
            throw new RuntimeException(e);
        }
        return returnValue;
    }
}
