package com.meiya;

import lombok.extern.slf4j.Slf4j;

import java.util.List;


/**
 * @author xiaopf
 */
@Slf4j
public class XrpcBootstrap {

    //XrpcBootstrap是单例 采用饿汉式方法创建

    private static XrpcBootstrap xrpcBootstrap = new XrpcBootstrap();

    private XrpcBootstrap(){
        //初始化
    }

    public static XrpcBootstrap getInstance(){
        return xrpcBootstrap;
    }


    /**
     * 定义当前应用的名称
     * @param applicationName   应用名称
     * @return  当前实例
     */
    public XrpcBootstrap application(String applicationName) {
        return this;
    }

    /**
     * 配置注册中心
     * @param registryConfig 注册中心的封装
     * @return 当前实例
     */
    public XrpcBootstrap registry(RegistryConfig registryConfig) {
        return this;
    }

    /**
     * 配置协议
     * @param protocolConfig 协议的封装
     * @return 当前实例
     */
    public XrpcBootstrap protocol(ProtocolConfig protocolConfig) {
        return this;
    }

    /**
     * 启动netty服务
     */
    public void start() {

    }


    //----------------------------------provider相关的api--------------------------------------

    /**
     * 服务发布
     * @param serviceConfig 需要发布的服务的封装
     * @return 当前实例
     */
    public XrpcBootstrap publish(ServiceConfig<?> serviceConfig) {
        return this;
    }

    /**
     * 批量 服务发布
     * @param serviceConfigList 需要发布的服务的封装的集合
     * @return 当前实例
     */
    public XrpcBootstrap publish(List<ServiceConfig> serviceConfigList){
        return this;
    }


    //----------------------------------consumer相关的api--------------------------------------


    public XrpcBootstrap reference(ReferenceConfig<?> reference) {

        return this;
    }
}
