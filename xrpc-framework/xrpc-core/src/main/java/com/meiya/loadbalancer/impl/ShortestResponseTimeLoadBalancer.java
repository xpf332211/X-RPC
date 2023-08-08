package com.meiya.loadbalancer.impl;

import com.meiya.XrpcBootstrap;
import com.meiya.loadbalancer.AbstractLoadBalancer;
import com.meiya.loadbalancer.Selector;
import io.netty.channel.Channel;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.Map;

/**
 * @author xiaopengfei
 */
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
                return serviceList.get(0);
            }
            List<Channel> channels = entry.getValue();
            //集合长度为1 直接返回主机
            if (channels.size() == 1){
                return (InetSocketAddress) channels.get(0);
            }else if (channels.size() > 1){
                //集合长度大于1 选择一个发布了该服务的主机
                for (Channel channel : channels){
                    InetSocketAddress inetSocketAddress = (InetSocketAddress)channel;
                    if (serviceList.contains(inetSocketAddress)){
                        return inetSocketAddress;
                    }
                }
            }
            throw new RuntimeException("未发现可用的主机！");
        }
    }
}
