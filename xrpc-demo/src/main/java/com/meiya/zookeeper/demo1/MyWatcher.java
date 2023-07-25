package com.meiya.zookeeper.demo1;

import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;

/**
 * @author xiaopf
 */
public class MyWatcher implements Watcher {
    @Override
    /**
     * 列举出几个监听
     */
    public void process(WatchedEvent event) {
        //判断类型 是否为连接类型事件 是否为节点创建事件  是否为节点删除事件 etc..
        if (event.getType() == Event.EventType.None){
            //判断状态 是什么连接状态
            if (event.getState() == Event.KeeperState.SyncConnected){
                System.out.println("zookeeper连接成功");
            }else if (event.getState() == Event.KeeperState.AuthFailed){
                System.out.println("zookeeper认证失败");
            }else if (event.getState() == Event.KeeperState.Disconnected){
                System.out.println("zookeeper断开连接");
            }
        }else if (event.getType() == Event.EventType.NodeCreated){
            System.out.println(event.getPath() + "节点创建");
        }else if (event.getType() == Event.EventType.NodeDeleted){
            System.out.println(event.getPath() + "节点删除");
        }else if (event.getType() == Event.EventType.NodeDataChanged){
            System.out.println(event.getPath() + "节点数据改变");
        }else if (event.getType() == Event.EventType.NodeChildrenChanged){
            System.out.println(event.getPath() + "子节点改变");
        }
    }
}
