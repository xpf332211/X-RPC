package com.meiya.proxy;

import com.meiya.annotation.XrpcService;
import com.meiya.exceptions.DiscoveryException;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Field;

/**
 * @author xiaopengfei
 */
@Slf4j
public class RpcBeanFactory {
    public static Object getRpcBean(Object bean) throws IllegalAccessException {
        //扫描所有bean 获取包含XrpcService注解的成员变量
        //获取代理对象 反射赋值
        Field[] fields = bean.getClass().getDeclaredFields();
        for (Field field : fields) {
            XrpcService annotation = field.getAnnotation(XrpcService.class);
            if (annotation != null){
                String group = annotation.group();
                field.setAccessible(true);
                Class<?> type = field.getType();
                Object proxy = null;
                try{
                    proxy = ProxyFactory.getProxy(type,group);
                }catch (DiscoveryException e){
                    log.error("指定的分组上无法获取到【{}】服务,请检查！",type);
                }
                field.set(bean,proxy);
            }
        }
        return bean;
    }


}
