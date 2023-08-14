package com.meiya.config;

import com.meiya.proxy.RpcBeanFactory;
import lombok.SneakyThrows;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

/**
 * RpcBeanFactory.getRpcBean提供重载方法 (Object bean,String group) 指定需要调用的服务所在分组
 * 对于非核心服务，默认放在default分组，无需指定
 * 对于核心服务，需要特别指定分组 否则报错
 * @author xiaopengfei
 */
@Component
public class XrpcConfig implements BeanPostProcessor {

    @SneakyThrows
    @Override
    //在bean初始化后增强
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        return RpcBeanFactory.getRpcBean(bean);
    }
}
