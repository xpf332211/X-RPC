package com.meiya;

import com.meiya.loadbalancer.LoadBalancer;
import com.meiya.loadbalancer.impl.RoundRobinLoadBalancer;
import com.meiya.registry.Registry;
import com.meiya.utils.IdGenerator;
import lombok.Data;

/**
 * 代码配置 --> xml配置 --> spi配置 --> 默认配置
 * @author xiaopengfei
 */
@Data
public class XrpcBootstrapConfiguration {
    /**
     * 服务提供方 主机端口
     */
    private int port = 8086;
    /**
     * 序列化类型 默认为jdk
     */
    private String serializeType = "jdk";

    /**
     * 压缩类型 默认为gzip
     */
    private String compressorType = "gzip";

    /**
     * 服务名称
     */
    private String applicationName = "defaultAppName";
    /**
     * 注册中心连接地址 默认为zk连接地址
     */
    private RegistryConfig registryConfig = new RegistryConfig("zookeeper://127.0.0.1:2181");
    /**
     * 注册中心实例 默认为zk 在构造器中实例化赋值
     */
    private Registry registry;

    /**
     * id生成器
     */
    private IdGenerator idGenerator = new IdGenerator(2, 10);
    /**
     * 负载均衡器
     */
    private LoadBalancer loadBalancer = new RoundRobinLoadBalancer();

    public XrpcBootstrapConfiguration() {
        //注意：register不能在成员变量中赋值！
        registry = registryConfig.getRegistry();
    }
}
