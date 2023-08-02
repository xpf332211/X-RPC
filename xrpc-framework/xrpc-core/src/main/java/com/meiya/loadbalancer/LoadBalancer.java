package com.meiya.loadbalancer;

import java.net.InetSocketAddress;

/**
 * 负载均衡器 接口
 * @author xiaopf
 */
public interface LoadBalancer {
    /**
     * 根据服务名称 采用负载均衡 获取一个可用服务
     * @param serviceName 服务名称
     * @return 可用服务地址
     */
    InetSocketAddress getServiceAddress(String serviceName);
}
