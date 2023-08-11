package com.meiya.config.loader;

import com.meiya.config.RegistryConfig;
import com.meiya.config.XrpcBootstrapConfiguration;
import com.meiya.loadbalancer.impl.RoundRobinLoadBalancer;
import com.meiya.utils.IdGenerator;

/**
 * @author xiaopf
 */
public class DefaultLoader {
    /**
     * 加载默认配置
     */
    public static void loadFromDefault(XrpcBootstrapConfiguration configuration) {
        //注意：register不能在成员变量中赋值！否则会有问题 且先赋值registryConfig后才能赋值registry
        configuration.setPort(8848);
        configuration.setApplicationName("default-appName");
        configuration.setSerializeType("jdk");
        configuration.setCompressType("gzip");
        configuration.setRegistryConfig(new RegistryConfig("zookeeper://127.0.0.1:2181"));
        configuration.setIdGenerator(new IdGenerator(0, 0));
        configuration.setLoadBalancer(new RoundRobinLoadBalancer());
        configuration.setRegistry(configuration.getRegistryConfig().getRegistry());
    }
}
