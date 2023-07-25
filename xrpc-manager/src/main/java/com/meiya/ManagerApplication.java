package com.meiya;


import com.meiya.utils.ZookeeperUtil;
import com.meiya.utils.zk.ZookeeperNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import java.util.List;


/**
 * 注册中心的管理
 *
 * @author xiaopf
 */
@Slf4j
public class ManagerApplication {
    /**
     * // xrpc-metadata   (持久节点)
     * //  └─ providers （持久节点）
     * //  		└─ service1  （持久节点，接口的全限定名）
     * //  		    ├─ node1 [data]     /ip:port套接字
     * //  		    ├─ node2 [data]
     * //           └─ node3 [data]
     * //  └─ consumers
     * //        └─ service1
     * //             ├─ node1 [data]
     * //             ├─ node2 [data]
     * //             └─ node3 [data]
     * //  └─ config
     *
     * @param args args
     */

    public static void main(String[] args) {
        //定义创建节点参数
        String bathPath = "/xrpc-metadata";
        String providersPath = bathPath + "/providers";
        String consumersPath = bathPath + "/consumers";
        //创建zk实例，建立连接
        ZooKeeper zookeeper = ZookeeperUtil.createZookeeper();
        //定义节点和数据
        ZookeeperNode baseNode = new ZookeeperNode(bathPath, null);
        ZookeeperNode providersNode = new ZookeeperNode(providersPath, null);
        ZookeeperNode consumersNode = new ZookeeperNode(consumersPath, null);
        List.of(baseNode, providersNode, consumersNode).forEach(node -> {
            ZookeeperUtil.createNode(zookeeper,node,null,null,null);
        });
        ZookeeperUtil.closeZookeeper(zookeeper);

    }
}