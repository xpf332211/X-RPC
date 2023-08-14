package com.meiya.bootstrap;

import com.meiya.utils.ZookeeperUtils;
import com.meiya.utils.zk.ZookeeperNode;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;

/**
 * @author xiaopengfei
 */
public class ZookeeperManageStarter {
    public static void start(){
        String bathPath = "/xrpc-metadata";
        String providersPath = bathPath + "/providers";
        ZooKeeper zookeeper = ZookeeperUtils.createZookeeper();
        ZookeeperNode baseNode = new ZookeeperNode(bathPath, null);
        ZookeeperNode providersNode = new ZookeeperNode(providersPath, null);
        List.of(baseNode, providersNode).forEach(node -> {
            ZookeeperUtils.createNode(zookeeper,node,null,null,null);
        });
    }
}
