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
            throw new NullPointerException("��������Ҫ�����ķ���ӿ�");
        }
        if (serviceConfig.getRef() == null){
            throw new NullPointerException("��������Ҫ�����ķ���ʵ����");
        }
        //���������Ӧ�ĸ��ڵ� Ϊ�־ýڵ�
        String serviceName = serviceConfig.getInterface().getName();
        String providersPath = Constant.BATH_PROVIDERS_PATH + '/' + serviceName;
        if (!ZookeeperUtils.exists(zooKeeper,providersPath,null)){
            ZookeeperNode zookeeperNode = new ZookeeperNode(providersPath,null);
            ZookeeperUtils.createNode(zooKeeper,zookeeperNode,null);
        }
        //���������Ӧ���ӽڵ� Ϊ��ʱ�ڵ� ����Ϊip:port
        //�����ṩ���Ķ˿���ֱ�Ӷ���� ����Ҫһ����ȡip�ķ���
        String childServiceName = providersPath + '/' + NetUtils.getIp() + ':' + port;
        if (!ZookeeperUtils.exists(zooKeeper,childServiceName,null)){
            ZookeeperNode zookeeperNode = new ZookeeperNode(childServiceName,null);
            ZookeeperUtils.createNode(zooKeeper,zookeeperNode,event -> {
                if (event.getType() == Watcher.Event.EventType.NodeDeleted){
                    log.info("�ڵ㡾{}���Ѿ���ɾ��",childServiceName);
                }
            },null, CreateMode.EPHEMERAL);
        }
    }
}
