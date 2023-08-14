package com.meiya.proxy;

import com.meiya.bootstrap.XrpcBootstrap;
import com.meiya.config.ReferenceConfig;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xiaopengfei
 */
public class ProxyFactory {
    private static final Map<Class<?> ,Object> PROXY_CACHE = new ConcurrentHashMap<>(16);
    public static <T> T getProxy(Class<T> clazz,String group){
        T proxy = (T) PROXY_CACHE.get(clazz);
        if (proxy != null){
            return proxy;
        }
        ReferenceConfig<T> referenceConfig = new ReferenceConfig<>();
        referenceConfig.setInterface(clazz);
        referenceConfig.setGroup(group);
        XrpcBootstrap.getInstance()
                .reference(referenceConfig)
                .finish();
        proxy = referenceConfig.get();
        return proxy;
    }
}
