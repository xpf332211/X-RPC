package com.meiya.config;

import com.meiya.config.loader.DefaultLoader;
import com.meiya.config.loader.SpiLoader;
import com.meiya.config.loader.XmlLoader;
import com.meiya.config.loader.YmlLoader;
import com.meiya.loadbalancer.LoadBalancer;
import com.meiya.registry.Registry;
import com.meiya.utils.IdGenerator;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * 代码配置 --> xml配置 --> spi配置 --> 默认配置
 *
 * @author xiaopengfei
 */
@Slf4j
@Data
public class XrpcBootstrapConfiguration {
    /**
     * 服务提供方 主机端口
     */
    private int port;
    /**
     * 服务名称
     */
    private String applicationName;
    /**
     * 序列化类型 默认为jdk
     */
    private String serializeType;

    /**
     * 压缩类型 默认为gzip
     */
    private String compressType;
    /**
     * 注册中心连接地址 默认为zk连接地址
     */
    private RegistryConfig registryConfig;
    /**
     * 注册中心实例 默认为zk 在构造器中实例化赋值
     */
    private Registry registry;
    /**
     * id生成器
     */
    private IdGenerator idGenerator;
    /**
     * 负载均衡器
     */
    private LoadBalancer loadBalancer;

    /**
     * 在XrpcBootstrap的构造器初始化时，会new Configuration初始化 依次读取 默认、spi、xml，后者若有值会覆盖前者
     * 初始化后Configuration初始化，在XrpcBootstrap也初始化完毕，调用其实例参数配置方法后，会覆盖先前的配置
     * 这样便实现了按优先级读取  代码配置 --> xml配置 --> spi配置 --> 默认配置
     */
    public XrpcBootstrapConfiguration() {
        //加载默认配置
        DefaultLoader.loadFromDefault(this);
        //加载spi配置
        SpiLoader.loadFromSpi(this);
        //加载xml配置
        XmlLoader.loadFromXml(this);
        //加载yml配置
        YmlLoader.loadFromYml(this);
    }
}
