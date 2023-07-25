package com.meiya.zookeeper;

import com.meiya.zookeeper.demo1.MyWatcher;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.events.Event;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class ZkTest {
    ZooKeeper zooKeeper;
    CountDownLatch countDownLatch = new CountDownLatch(1);

    /**
     * 实例化zookeeper
     */
    @Before
    public void createZookeeper() {
        String connectString = "127.0.0.1:2181";
        int sessionTimeout = 10 * 1000;
        //zookeeper的默认监听器
        Watcher watcher = new MyWatcher();
        try {
            zooKeeper = new ZooKeeper(connectString, sessionTimeout, event -> {
                //只有连接成功才放行
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected){
                    System.out.println("zookeeper连接成功！");
                    countDownLatch.countDown();
                }
            });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 创建zookeeper节点
     */
    @Test
    public void createZookeeperNode() {
        String path = "/xrpc";
        byte[] data = "hello".getBytes(StandardCharsets.UTF_8);
        List<ACL> acl = ZooDefs.Ids.OPEN_ACL_UNSAFE;
        CreateMode createMode = CreateMode.CONTAINER;
        try {
            //等待连接成功
            countDownLatch.await();
            String result = zooKeeper.create(path, data, acl, createMode);
            System.out.println("result = " + result);
        } catch (KeeperException | InterruptedException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (zooKeeper != null) {
                    zooKeeper.close();
                }
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }


    @Test
    public void deleteZookeeperNode(){
        String path = "/xrpc";
        //版本号法做乐观锁 避免多线程下的删除问题 此处不使用
        int version = -1;
        try {
            zooKeeper.delete(path,version);
            System.out.println("zk删除");
        } catch (InterruptedException | KeeperException e) {
            throw new RuntimeException(e);
        }finally {
            if (zooKeeper != null){
                try {
                    zooKeeper.close();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Test
    public void getVersion(){
        String path = "/xrpc";
        try {
            //获取版本 可以用来做乐观锁
            Stat stat = zooKeeper.exists(path, null);
            //当前节点的数据版本
            int version = stat.getVersion();
            //当前节点的ACL版本
            int aclVersion = stat.getAversion();
            //当前节点的子节点的数据版本
            int childVersion = stat.getCversion();
        } catch (KeeperException | InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            if (zooKeeper != null){
                try {
                    zooKeeper.close();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    @Test
    public void testWatcher(){
        String path = "/xrpc";
        try {
            //可主动提供watcher实现，也可指定true 则使用zookeeper实例化时的watcher
            zooKeeper.exists(path, true);
            //不让程序退出 测试watcher
            while (true){
                Thread.sleep(10 * 1000);
            }
        } catch (KeeperException | InterruptedException e) {
            throw new RuntimeException(e);
        }finally {
            try {
                zooKeeper.close();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }


}
