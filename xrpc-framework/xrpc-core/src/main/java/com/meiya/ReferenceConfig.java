package com.meiya;


import com.meiya.exceptions.NettyException;
import com.meiya.proxy.handler.RpcConsumerInvocationHandler;
import com.meiya.registry.Registry;
import com.meiya.utils.print.Out;
import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * @author xiaopf
 */
@Slf4j
public class ReferenceConfig<T> {
    private Class<T> interfaceRef;
    private Registry registry;


    public Class<T> getInterface() {
        return interfaceRef;
    }

    public void setInterface(Class<T> interfaceRef) {
        this.interfaceRef = interfaceRef;
    }

    public Registry getRegistry() {
        return registry;
    }

    public void setRegistry(Registry registry) {
        this.registry = registry;
    }

    /**
     * 代理设计模式 生成一个api接口的代理对象
     *
     * @return 代理对象
     */
    public T get() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class<?>[] classes = new Class[]{interfaceRef};
        InvocationHandler invocationHandler = new RpcConsumerInvocationHandler(registry,interfaceRef);
        //使用动态代理 生成代理对象
        Object proxy = Proxy.newProxyInstance(classLoader, classes, invocationHandler);
        return (T) proxy;
    }


}
