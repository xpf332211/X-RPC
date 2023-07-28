package com.meiya.registry;

import com.meiya.ServiceConfig;

import java.net.InetSocketAddress;

/**
 * @author xiaopf
 */
public interface Registry {
    /**
     * 注册中心的注册实例方法
     * @param serviceConfig 服务信息配置
     */
    void register(ServiceConfig<?> serviceConfig);

    /**
     * 从注册中心拉取一个可用的服务
     * @param serviceName 服务名称
     * @return 服务地址
     */
    InetSocketAddress seek(String serviceName);
}
