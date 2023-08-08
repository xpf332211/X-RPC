package com.meiya;

import com.meiya.detection.HeartbeatDetector;
import com.meiya.utils.print.Out;
import lombok.extern.slf4j.Slf4j;

/**
 * @author xiaopf
 */
public class ConsumerApplication {
    public static void main(String[] args) {
        //获取一个reference 把配置项封装
        ReferenceConfig<MessageService> referenceConfig = new ReferenceConfig<>();
        referenceConfig.setInterface(MessageService.class);
        ReferenceConfig<ProductService> referenceConfig1 = new ReferenceConfig<>();
        referenceConfig1.setInterface(ProductService.class);

        /**
         * 代理做了什么？
         * 1.连接到注册中心
         * 2.从注册中心拉取服务列表
         * 3.选择一个服务并建立连接
         * 4.发送请求，携带信息，获得结果
         */
        XrpcBootstrap.getInstance()
                .application("first-consumer")
                .registry(new RegistryConfig("zookeeper://127.0.0.1:2181"))
                .reference(referenceConfig)
                .reference(referenceConfig1)
                .serialize("json")
                .compress("gzip")
                .finish();

        //获取代理对象
        MessageService messageService = referenceConfig.get();
        String message = messageService.getMessage("Jerry");
        Out.println(message);


    }
}
