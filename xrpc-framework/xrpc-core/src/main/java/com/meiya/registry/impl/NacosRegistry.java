package com.meiya.registry.impl;

import com.meiya.config.ServiceConfig;
import com.meiya.registry.AbstractRegistry;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author xiaopf
 */
public class NacosRegistry extends AbstractRegistry {
    public NacosRegistry(String connectStr, int sessionTimeout) {

    }

    @Override
    public void register(ServiceConfig serviceConfig) {

    }

    @Override
    public List<InetSocketAddress> seekServiceList(String serviceName) {
        return null;
    }


}
