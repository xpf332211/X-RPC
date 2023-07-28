package com.meiya.registry.impl;

import com.meiya.ServiceConfig;
import com.meiya.registry.AbstractRegistry;

import java.net.InetSocketAddress;

/**
 * @author xiaopf
 */
public class NacosRegistry extends AbstractRegistry {
    public NacosRegistry(String connectStr, int sessionTimeout) {

    }

    @Override
    public void register(ServiceConfig<?> serviceConfig) {

    }

    @Override
    public InetSocketAddress seek(String serviceName) {
        return null;
    }
}
