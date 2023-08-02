package com.meiya.loadbalancer;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author xiaopf
 */
public interface Selector {

    /**
     * 负载均衡策略
     * @return 可用的服务地址
     */
    InetSocketAddress loadBalance();
}
