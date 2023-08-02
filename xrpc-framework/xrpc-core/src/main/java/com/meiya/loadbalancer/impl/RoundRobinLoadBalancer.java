package com.meiya.loadbalancer.impl;

import com.meiya.XrpcBootstrap;
import com.meiya.exceptions.DiscoveryException;
import com.meiya.exceptions.LoadBalanceException;
import com.meiya.loadbalancer.AbstractLoadBalancer;
import com.meiya.loadbalancer.LoadBalancer;
import com.meiya.loadbalancer.Selector;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author xiaopf
 */
public class RoundRobinLoadBalancer extends AbstractLoadBalancer {


    @Override
    protected Selector initSelector(List<InetSocketAddress> serviceList) {
        return new RoundRobinSelector(serviceList);
    }

    private static class RoundRobinSelector implements Selector{

        /**
         * 待选取的服务列表
         */
        private final List<InetSocketAddress> serviceList;
        /**
         * 服务列表的游标
         */
        private final AtomicInteger index;
        public RoundRobinSelector(List<InetSocketAddress> serviceList){
            this.serviceList = serviceList;
            index = new AtomicInteger(0);
        }

        //轮询算法
        @Override
        public InetSocketAddress loadBalance() {
            InetSocketAddress service = serviceList.get(index.get());
            if (index.get() == serviceList.size() - 1){
                index.set(0);
            }else {
                index.incrementAndGet();
            }
            return service;
        }
    }
}
