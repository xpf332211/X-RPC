package com.meiya.config;

import com.meiya.annotation.XrpcService;
import com.meiya.proxy.ProxyFactory;
import lombok.SneakyThrows;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;

/**
 * @author xiaopengfei
 */
@Component
public class XrpcProxyBeanPostProcessor implements BeanPostProcessor {

    @SneakyThrows
    @Override
    //在bean初始化后增强
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        //扫描注解
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            XrpcService annotation = field.getAnnotation(XrpcService.class);
            if (annotation != null){
                field.setAccessible(true);
                Class<?> type = field.getType();
                Object proxy = ProxyFactory.getProxy(type, "default2");
                field.set(bean,proxy);
            }
        }

        return bean;
    }
}
