package com.meiya.loadbalancer;

import com.meiya.bootstrap.XrpcBootstrap;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author xiaopf
 */
public abstract class AbstractLoadBalancer implements LoadBalancer{
    /**
     * 选择器的缓存 每个服务对应一个选择器
     */
    private static final Map<String,Selector> SELECTOR_CACHE = new ConcurrentHashMap<>(8);
    @Override
    public InetSocketAddress getServiceAddress(String serviceName,String group) {
        //从缓存中获取服务对应的选择器
        Selector selector = SELECTOR_CACHE.get(serviceName);
        if (selector == null){
            List<InetSocketAddress> serviceList = XrpcBootstrap.getInstance().getConfiguration().getRegistry().seekServiceList(serviceName,group);
            selector = initSelector(serviceList);
            SELECTOR_CACHE.put(serviceName,selector);
        }
        return selector.loadBalance();
    }

    @Override
    public void reLoadBalance(String serviceName,List<InetSocketAddress> addressList) {
        if (serviceName != null){
            SELECTOR_CACHE.put(serviceName,initSelector(addressList));
        }

    }

    /**
     *  由子类返回一个selector实现类的实例
     * @param serviceList new实例时需要的参数
     * @return selector实现类的实例
     */
    protected abstract Selector initSelector(List<InetSocketAddress> serviceList);
}
