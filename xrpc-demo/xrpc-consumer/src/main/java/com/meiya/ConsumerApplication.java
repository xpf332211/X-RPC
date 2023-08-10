package com.meiya;

import com.meiya.detection.HeartbeatDetector;
import com.meiya.utils.print.Out;
import lombok.extern.slf4j.Slf4j;

/**
 * @author xiaopf
 */
public class ConsumerApplication {
    public static void main(String[] args) throws InterruptedException {
        //获取一个reference 把配置项封装
        ReferenceConfig<MessageService> referenceConfig = new ReferenceConfig<>();
        referenceConfig.setInterface(MessageService.class);
        ReferenceConfig<ProductService> referenceConfig1 = new ReferenceConfig<>();
        referenceConfig1.setInterface(ProductService.class);

        XrpcBootstrap.getInstance()
                .reference(referenceConfig)
                .reference(referenceConfig1)
                .finish();

        //获取代理对象
       while (true){
           Thread.sleep(1 * 1000);
           MessageService messageService = referenceConfig.get();
           String message = messageService.getMessage("Jerry");
       }
    }
}
