package com.meiya.registry;

import com.meiya.config.ServiceConfig;

import java.net.InetSocketAddress;
import java.util.List;

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
     * 从注册中心拉取服务列表
     * @param serviceName 服务名称
     * @param group 服务分组
     * @return 服务地址列表
     */
    List<InetSocketAddress> seekServiceList(String serviceName,String group);
}
