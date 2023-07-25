package com.meiya;


import java.lang.reflect.Proxy;

/**
 * @author xiaopf
 */

public class ReferenceConfig<T> {
    private Class<T> interfaceRef;

    public Class<T> getInterface() {
        return interfaceRef;
    }

    public void setInterface(Class<T> interfaceConsumer) {
        this.interfaceRef = interfaceConsumer;
    }

    public T get() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class[] classes = new Class[]{interfaceRef};
        //使用动态代理 生成代理对象
        Object proxy = Proxy.newProxyInstance(classLoader, classes, (proxy1, method, args) -> {
            System.out.println("proxy!");
            return null;
        });
        return (T) proxy;
    }
}
