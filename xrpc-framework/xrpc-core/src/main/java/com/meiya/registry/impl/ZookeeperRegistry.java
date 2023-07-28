package com.meiya.registry.impl;

import com.meiya.Constant;
import com.meiya.ServiceConfig;
import com.meiya.exceptions.DiscoveryException;
import com.meiya.registry.AbstractRegistry;
import com.meiya.registry.Registry;
import com.meiya.utils.NetUtils;
import com.meiya.utils.ZookeeperUtils;
import com.meiya.utils.zk.ZookeeperNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

import java.net.InetSocketAddress;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author xiaopf
 */
@Slf4j
public class ZookeeperRegistry extends AbstractRegistry {
    private final ZooKeeper zooKeeper;
    int port = 8080;
    public ZookeeperRegistry() {
        zooKeeper = ZookeeperUtils.createZookeeper();
    }
    public ZookeeperRegistry(String connectStr,int sessionTimeout) {
        zooKeeper = ZookeeperUtils.createZookeeper(connectStr,sessionTimeout);
    }


    @Override
    public void register(ServiceConfig<?> serviceConfig) {
        if (serviceConfig.getInterface() == null){
            throw new NullPointerException("请配置需要发布的服务接口");
        }
        if (serviceConfig.getRef() == null){
            throw new NullPointerException("请配置需要发布的服务实现类");
        }
        //创建服务对应的根节点 为持久节点
        String serviceName = serviceConfig.getInterface().getName();
        String providersPath = Constant.BATH_PROVIDERS_PATH + '/' + serviceName;
        if (!ZookeeperUtils.exists(zooKeeper,providersPath,null)){
            ZookeeperNode zookeeperNode = new ZookeeperNode(providersPath,null);
            ZookeeperUtils.createNode(zooKeeper,zookeeperNode,null);
        }
        //创建服务对应的子节点 为临时节点 名称为ip:port
        //服务提供方的端口先直接定义好 还需要一个获取ip的方法
        String childServiceName = providersPath + '/' + NetUtils.getIp() + ':' + port;
        if (!ZookeeperUtils.exists(zooKeeper,childServiceName,null)){
            ZookeeperNode zookeeperNode = new ZookeeperNode(childServiceName,null);
            ZookeeperUtils.createNode(zooKeeper,zookeeperNode,null,null, CreateMode.EPHEMERAL);
        }
    }

    @Override
    public InetSocketAddress seek(String serviceName) {
        String servicePath = Constant.BATH_PROVIDERS_PATH + '/' + serviceName;
        //获取子节点
        List<String> childrenService = ZookeeperUtils.getChildren(zooKeeper,servicePath,null);
        List<InetSocketAddress> inetSocketAddressList = childrenService.stream().map(host -> {
            String[] ipAndPort = host.split(":");
            String ip = ipAndPort[0];
            int port = Integer.parseInt(ipAndPort[1]);
            return new InetSocketAddress(ip,port);
        }).collect(Collectors.toList());
        if (inetSocketAddressList.size() == 0){
            throw new DiscoveryException("未获取到可用的子节点");
        }
        return inetSocketAddressList.get(0);
    }
}
