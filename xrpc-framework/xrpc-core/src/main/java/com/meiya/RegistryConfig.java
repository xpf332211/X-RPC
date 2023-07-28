package com.meiya;

import com.meiya.exceptions.RegistryException;
import com.meiya.registry.Registry;
import com.meiya.registry.impl.NacosRegistry;
import com.meiya.registry.impl.ZookeeperRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author xiaopf
 */
public class RegistryConfig {
    private final String connect;
    public RegistryConfig(String connect) {
        this.connect = connect;
    }

    public Registry getRegistry() {
        Map<String,String> registryMap = new HashMap<>(4){
            {
                put("zookeeper","zookeeper");
                put("nacos","nacos");
            }
        };
        String type = getTypeOrHost(connect, true);
        if (!registryMap.containsKey(type)){
            throw new RegistryException("未匹配到指定的注册中心!");
        }
        if (Objects.equals(registryMap.get(type), "zookeeper")){
            String host = getTypeOrHost(connect, false);
            return new ZookeeperRegistry(host,Constant.DEFAULT_ZK_TIMEOUT);
        }else if (Objects.equals(registryMap.get(type), "nacos")){
            String host = getTypeOrHost(connect, false);
            return new NacosRegistry(host,Constant.DEFAULT_ZK_TIMEOUT);
        }else {
            throw new RegistryException("未匹配到指定的注册中心!");
        }


    }

    private String getTypeOrHost(String connect,boolean isType){
        int splitLen = 2;
        String[] strings = connect.split("://");
        if (strings.length != splitLen){
            throw new RegistryException("提供的注册中心连接url不合法！");
        }
        if (isType){
            return strings[0].toLowerCase().trim();
        }else {
            return strings[1].toLowerCase().trim();
        }
    }
}
