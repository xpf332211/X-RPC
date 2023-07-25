package com.meiya;

import com.meiya.impl.MessageServiceImpl;


/**
 * @author xiaopf
 */
public class ProviderApplication {
    public static void main(String[] args) {

        //封装要发布的服务
        ServiceConfig<MessageService> messageServiceConfig = new ServiceConfig<>();
        messageServiceConfig.setInterface(MessageService.class);
        messageServiceConfig.setRef(new MessageServiceImpl());
        //启动引导 配置
        //应用名称 注册中心 协议
        //发布服务
        XrpcBootstrap.getInstance()
                .application("first-provider")
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .protocol(new ProtocolConfig("jdk"))
                .publish(messageServiceConfig)
                .start();
    }
}
