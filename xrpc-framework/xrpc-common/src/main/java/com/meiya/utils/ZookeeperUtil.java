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
     * ����zkʵ��
     * @param connectString �����׽���
     * @param sessionTimeout ���ӳ�ʱʱ��
     * @return zkʵ��
     */
    public static ZooKeeper createZookeeper(String connectString, int sessionTimeout) {
        ZooKeeper zooKeeper = null;
        CountDownLatch countDownLatch = new CountDownLatch(1);

        try {
            //����zkʵ������������
            zooKeeper = new ZooKeeper(connectString, sessionTimeout, event -> {
                //ֻ�����ӳɹ��ŷ���
                if (event.getState() == Watcher.Event.KeeperState.SyncConnected) {
                    log.info("zookeeper���ӳɹ���");
                    countDownLatch.countDown();
                }
            });
            return zooKeeper;
        } catch (IOException e) {
            log.error("����zookeeperʵ��ʱ�����쳣��{}", e.getMessage());
            throw new ZookeeperException();
        }
    }

    /**
     * ������Ĭ�ϲ�����zkʵ��
     * @return zkʵ��
     */
    public static ZooKeeper createZookeeper() {
        //����Ĭ�����Ӳ���
        String connectString = Constant.DEFAULT_ZK_CONNECT;
        int sessionTimeout = Constant.DEFAULT_ZK_TIMEOUT;
        return createZookeeper(connectString, sessionTimeout);

    }

    /**
     * ����һ��Ŀ¼�ڵ�
     * @param zooKeeper zkʵ��
     * @param node Ŀ¼�ڵ�
     * @param watcher watcher
     * @param acl ����Ȩ��
     * @param createMode �ڵ�����
     * @return �����ɹ�-->true  ����ʧ��|���쳣-->false
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
                log.info("�ڵ㡾{}���ɹ�������", result);
                return true;
            } else {
                log.info("�ڵ㡾{}���Ѿ����ڣ����贴����", node.getNodePath());
            }
        } catch (KeeperException | InterruptedException e) {
            log.error("��������Ŀ¼ʱ�����쳣��");
            throw new ZookeeperException();
        }
        return false;
    }
}
