package com.meiya.loadbalancer;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * 负载均衡器 接口
 * @author xiaopf
 */
public interface LoadBalancer {
    /**
     * 根据服务名称 采用负载均衡 获取一个可用服务
     * @param serviceName 服务名称
     * @param group 服务分组
     * @return 可用服务地址
     */
    InetSocketAddress getServiceAddress(String serviceName,String group);

    /**
     * 当感知节点动态上下线 需要重新负载均衡 拉取最新的服务列表
     * @param serviceName 需要重新拉取节点的服务名称
     * @param addressList 拉取后的结果
     */
    void reLoadBalance(String serviceName, List<InetSocketAddress> addressList);
}
