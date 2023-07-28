package com.meiya.registry.impl;

import com.meiya.Constant;
import com.meiya.ServiceConfig;
import com.meiya.registry.AbstractRegistry;
import com.meiya.registry.Registry;
import com.meiya.utils.NetUtils;
import com.meiya.utils.ZookeeperUtils;
import com.meiya.utils.zk.ZookeeperNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

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
            ZookeeperUtils.createNode(zooKeeper,zookeeperNode,event -> {
                if (event.getType() == Watcher.Event.EventType.NodeDeleted){
                    log.info("节点【{}】已经被删除",childServiceName);
                }
            },null, CreateMode.EPHEMERAL);
        }
    }
}
