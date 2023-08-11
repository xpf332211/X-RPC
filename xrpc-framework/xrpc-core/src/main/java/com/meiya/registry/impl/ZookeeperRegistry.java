package com.meiya.registry.impl;

import com.meiya.constant.ZookeeperConstant;
import com.meiya.config.ServiceConfig;
import com.meiya.bootstrap.XrpcBootstrap;
import com.meiya.exceptions.DiscoveryException;
import com.meiya.registry.AbstractRegistry;
import com.meiya.utils.NetUtils;
import com.meiya.utils.ZookeeperUtils;
import com.meiya.utils.zk.ZookeeperNode;
import com.meiya.watcher.OnlineAndOfflineWatcher;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooKeeper;

import java.net.InetSocketAddress;
import java.util.List;

/**
 * @author xiaopf
 */
@Slf4j
public class ZookeeperRegistry extends AbstractRegistry {
    private final ZooKeeper zooKeeper;
    public ZookeeperRegistry() {
        zooKeeper = ZookeeperUtils.createZookeeper();
    }
    public ZookeeperRegistry(String connectStr,int sessionTimeout) {
        zooKeeper = ZookeeperUtils.createZookeeper(connectStr,sessionTimeout);
    }



    @Override
    public void register(ServiceConfig serviceConfig) {
        if (serviceConfig.getInterface() == null){
            throw new NullPointerException("请配置需要发布的服务接口");
        }
        if (serviceConfig.getRef() == null){
            throw new NullPointerException("请配置需要发布的服务实现类");
        }
        //创建服务对应的根节点 为持久节点
        String serviceName = serviceConfig.getInterface().getName();
        String providersPath = ZookeeperConstant.BATH_PROVIDERS_PATH + '/' + serviceName;
        if (!ZookeeperUtils.exists(zooKeeper,providersPath,null)){
            ZookeeperNode zookeeperNode = new ZookeeperNode(providersPath,null);
            ZookeeperUtils.createNode(zooKeeper,zookeeperNode,null);
        }
        //创建服务对应的子节点 为临时节点 名称为ip:port
        //服务提供方的端口先直接定义好 还需要一个获取ip的方法
        String childServiceName = providersPath + '/' + NetUtils.getIp() + ':' + XrpcBootstrap.getInstance().getConfiguration().getPort();
        if (!ZookeeperUtils.exists(zooKeeper,childServiceName,null)){
            ZookeeperNode zookeeperNode = new ZookeeperNode(childServiceName,null);
            ZookeeperUtils.createNode(zooKeeper,zookeeperNode,null,null, CreateMode.EPHEMERAL);
        }
    }

    @Override
    public List<InetSocketAddress> seekServiceList(String serviceName) {
        String servicePath = ZookeeperConstant.BATH_PROVIDERS_PATH + '/' + serviceName;
        //获取子节点
        List<String> childrenService = ZookeeperUtils.getChildren(zooKeeper,servicePath,new OnlineAndOfflineWatcher());
        List<InetSocketAddress> inetSocketAddressList = childrenService.stream().map(host -> {
            String[] ipAndPort = host.split(":");
            String ip = ipAndPort[0];
            int port = Integer.parseInt(ipAndPort[1]);
            return new InetSocketAddress(ip,port);
        }).toList();
        if (inetSocketAddressList.isEmpty()){
            throw new DiscoveryException("未获取到可用的子节点");
        }
        return inetSocketAddressList;
    }


}
