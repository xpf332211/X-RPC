package com.meiya;

import com.meiya.registry.Registry;
import lombok.extern.slf4j.Slf4j;


import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


/**
 * @author xiaopf
 */
@Slf4j
public class XrpcBootstrap {


    private String applicationName;
    private RegistryConfig registryConfig;
    private ProtocolConfig protocolConfig;
    /**
     * 注册中心
     */
    private Registry registry;

    /**
     * 服务列表
     * key--> interface的全限名 value--> ServiceConfig
     */
    private static final Map<String,ServiceConfig<?>> SERVICE_MAP = new ConcurrentHashMap<>(16);
    /**
     * XrpcBootstrap是单例 采用饿汉式方法创建
     */
    private static final XrpcBootstrap XRPC_BOOTSTRAP = new XrpcBootstrap();

    private XrpcBootstrap(){
        //初始化
    }

    public static XrpcBootstrap getInstance(){
        return XRPC_BOOTSTRAP;
    }


    /**
     * 定义当前应用的名称
     * @param applicationName   应用名称
     * @return  当前实例
     */
    public XrpcBootstrap application(String applicationName) {
        this.applicationName = applicationName;
        return this;
    }

    /**
     * 配置注册中心
     * @param registryConfig 注册中心的封装
     * @return 当前实例
     */
    public XrpcBootstrap registry(RegistryConfig registryConfig) {
        this.registry = registryConfig.getRegistry();
        return this;
    }

    /**
     * 配置协议
     * @param protocolConfig 协议的封装
     * @return 当前实例
     */
    public XrpcBootstrap protocol(ProtocolConfig protocolConfig) {
        this.protocolConfig = protocolConfig;
        return this;
    }

    /**
     * 启动netty服务
     */
    public void start() {
        try {
            while (true){
                Thread.sleep(10 * 1000);
            }

        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }


    //----------------------------------provider相关的api--------------------------------------

    /**
     * 服务发布
     * @param serviceConfig 需要发布的服务的封装
     * @return 当前实例
     */
    public XrpcBootstrap publish(ServiceConfig<?> serviceConfig) {
        //将服务注册到注册中心上
        registry.register(serviceConfig);
        //维护服务列表
        SERVICE_MAP.put(serviceConfig.getInterface().getName(),serviceConfig);
        return this;
    }

    /**
     * 批量 服务发布
     * @param serviceConfigList 需要发布的服务的封装的集合
     * @return 当前实例
     */
    public XrpcBootstrap publish(List<ServiceConfig<?>> serviceConfigList){
        serviceConfigList.forEach(serviceConfig -> {
            registry.register(serviceConfig);
            SERVICE_MAP.put(serviceConfig.getInterface().getName(),serviceConfig);
        });
        return this;
    }


    //----------------------------------consumer相关的api--------------------------------------


    /**
     * 代理对象配置
     * @param referenceConfig 代理对象配置
     * @return 当前实例
     */
    public XrpcBootstrap reference(ReferenceConfig<?> referenceConfig) {
        referenceConfig.setRegistry(registry);
        return this;
    }
}
