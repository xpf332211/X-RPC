package com.meiya;

/**
 * @author xiaopf
 */
public class ConsumerApplication {
    public static void main(String[] args) {
        //获取一个reference 把配置项封装
        ReferenceConfig<MessageService> reference = new ReferenceConfig<>();
        reference.setInterface(MessageService.class);

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
                .reference(reference);

        //获取代理对象
        MessageService messageService = reference.get();
        String message = messageService.getMessage("Jerry");
    }
}
