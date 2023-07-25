package com.meiya.utils;

import com.meiya.Constant;
import com.meiya.exceptions.ZookeeperException;
import com.meiya.utils.zk.ZookeeperNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author xiaopf
 */
@Slf4j
public class ZookeeperUtil {

    /**
     * 创建zk实例
     * @param connectString 连接套接字
     * @param sessionTimeout 连接超时时间
     * @return zk实例
     */
    public static ZooKeeper createZookeeper(String connectString, int sessionTimeout) {
        ZooKeeper zooKeeper = null;
        CountDownLatch countDownLatch = new CountDownLatch(1);

        try {
            //创建zk实例，建立连接
            zooKeeper = new ZooKeeper(connectString, sessionTimeout, event -> {
                //只有连接成功才放行
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    log.info("zookeeper连接成功！");
                    countDownLatch.countDown();
                }
            });
            return zooKeeper;
        } catch (IOException e) {
            log.error("创建zookeeper实例时发生异常：{}", e.getMessage());
            throw new ZookeeperException();
        }
    }

    /**
     * 创建带默认参数的zk实例
     * @return zk实例
     */
    public static ZooKeeper createZookeeper() {
        //定义默认连接参数
        String connectString = Constant.DEFAULT_ZK_CONNECT;
        int sessionTimeout = Constant.DEFAULT_ZK_TIMEOUT;
        return createZookeeper(connectString, sessionTimeout);

    }

    /**
     * 创建一个目录节点
     * @param zooKeeper zk实例
     * @param node 目录节点
     * @param watcher watcher
     * @param acl 控制权限
     * @param createMode 节点类型
     * @return 创建成功-->true  创建失败|抛异常-->false
     */
    public static boolean createNode(ZooKeeper zooKeeper, ZookeeperNode node, Watcher watcher, List<ACL> acl, CreateMode createMode) {

        if (acl == null || acl.isEmpty()) {
            acl = ZooDefs.Ids.OPEN_ACL_UNSAFE;
        }
        if (createMode == null) {
            createMode = CreateMode.CONTAINER;
        }

        try {
            if (zooKeeper.exists(node.getNodePath(), null) == null) {
                String result = zooKeeper.create(node.getNodePath(), node.getData(), acl, createMode);
                log.info("节点【{}】成功创建！", result);
                return true;
            } else {
                log.info("节点【{}】已经存在，无需创建！", node.getNodePath());
            }
        } catch (KeeperException | InterruptedException e) {
            log.error("创建基础目录时发生异常！");
            throw new ZookeeperException();
        }
        return false;
    }
}
