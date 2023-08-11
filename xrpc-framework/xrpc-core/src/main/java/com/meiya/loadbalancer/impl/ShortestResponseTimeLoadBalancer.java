package com.meiya.loadbalancer.impl;

import com.meiya.bootstrap.XrpcBootstrap;
import com.meiya.loadbalancer.AbstractLoadBalancer;
import com.meiya.loadbalancer.Selector;
import io.netty.channel.Channel;
import lombok.extern.slf4j.Slf4j;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

/**
 * @author xiaopengfei
 */
@Slf4j
public class ShortestResponseTimeLoadBalancer extends AbstractLoadBalancer {
    @Override
    protected Selector initSelector(List<InetSocketAddress> serviceList) {
        return new ShortestResponseTimeSelector(serviceList);
    }

    private static class ShortestResponseTimeSelector implements Selector{


        //该服务下可用的主机
        private final List<InetSocketAddress> serviceList;
        public ShortestResponseTimeSelector(List<InetSocketAddress> serviceList) {
            this.serviceList = serviceList;
        }

        @Override
        public InetSocketAddress loadBalance() {
            //获取响应时间最短的主机集合
            Map.Entry<Long, List<Channel>> entry = XrpcBootstrap.RESPONSE_TIME_CHANNEL_CACHE.firstEntry();
            //如果第一次心跳检测还未完成就需要调用 则从可用主机中返回第一个
            if (entry == null){
                if (log.isDebugEnabled()){
                    log.debug("第一次心跳检测还未完成，不选取最短响应的主机");
                }
                return serviceList.get(0);
            }
            List<Channel> channels = entry.getValue();
            //对最短响应的所有主机进行遍历，若没有一台主机发布了该服务 则从可用主机中返回第一个
            for (Channel channel : channels){
                InetSocketAddress inetSocketAddress = (InetSocketAddress)channel.remoteAddress();
                if (serviceList.contains(inetSocketAddress)){
                    if (log.isDebugEnabled()){
                        log.debug("根据心跳检测的结果，选取了支持该服务的最短响应的主机");
                    }
                    return inetSocketAddress;
                }
            }
            if (log.isDebugEnabled()){
                log.debug("最短响应的主机不支持该服务，将从可用主机中选择一台");
            }
            return serviceList.get(0);
        }
    }
}
