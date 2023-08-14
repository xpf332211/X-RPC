package com.meiya.bootstrap;

import com.meiya.utils.ZookeeperUtils;
import com.meiya.utils.zk.ZookeeperNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.ZooKeeper;

import java.util.List;

/**
 * @author xiaopengfei
 */
@Slf4j
public class ScanServiceStarter {
    private final String scanPackage;
    public ScanServiceStarter(String scanPackage){
        this.scanPackage = scanPackage;
    }
    public void start() throws InterruptedException {
        Thread.sleep(1000);
        log.info("xrpc正在启动~");
        ZookeeperManageStarter.start();
        XrpcBootstrap.getInstance()
                .scan(scanPackage)
                .start();
    }
}
