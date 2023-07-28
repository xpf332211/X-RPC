package com.meiya;


import com.meiya.registry.Registry;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Proxy;
import java.net.InetSocketAddress;

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
     * @return 代理对象
     */
    public T get() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class[] classes = new Class[]{interfaceRef};
        //使用动态代理 生成代理对象
        Object proxy = Proxy.newProxyInstance(classLoader, classes, (proxy1, method, args) -> {
            InetSocketAddress address = registry.seek(interfaceRef.getName());
            System.out.println("引用");
            log.info("服务调用方,返回了服务【{}】的可用主机【{}】",interfaceRef.getName(),address);
            return null;
        });
        return (T) proxy;
    }


}
