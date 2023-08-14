package com.meiya.bootstrap;

import lombok.extern.slf4j.Slf4j;

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
        XrpcBootstrap.getInstance()
                .scan(scanPackage)
                .start();
    }
}
