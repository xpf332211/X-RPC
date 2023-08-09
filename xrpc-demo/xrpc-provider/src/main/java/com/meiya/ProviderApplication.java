package com.meiya;

import com.meiya.impl.MessageServiceImpl;

import com.meiya.impl.ProductServiceImpl;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;


/**
 * @author xiaopf
 */
@Slf4j
public class ProviderApplication {
    public static void main(String[] args) throws UnsupportedEncodingException {


        //封装要发布的服务 已经修改成包扫描 + 注解了
//        ServiceConfig messageServiceConfig = new ServiceConfig();
//        messageServiceConfig.setInterface(MessageService.class);
//        messageServiceConfig.setRef(new MessageServiceImpl());
//        ServiceConfig productServiceConfig = new ServiceConfig();
//        productServiceConfig.setInterface(ProductService.class);
//        productServiceConfig.setRef(new ProductServiceImpl());
        //启动引导 配置
        //应用名称 注册中心 协议
        //发布服务
        XrpcBootstrap.getInstance()
                .application("first-provider")
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .protocol(new ProtocolConfig("jdk"))
                .scan("com.meiya")
                .start();
        
    }
}
