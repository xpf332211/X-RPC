package com.meiya.registry;

import com.meiya.ServiceConfig;

/**
 * @author xiaopf
 */
public interface Registry {
    /**
     * 注册中心的注册实例方法
     * @param serviceConfig 服务信息配置
     */
    void register(ServiceConfig<?> serviceConfig);
}
