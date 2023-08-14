package com.meiya.config;

import com.meiya.bootstrap.XrpcBootstrap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

/**
 * @author xiaopengfei
 */
@Slf4j
@Component
public class XrpcStarter implements CommandLineRunner {
    @Override
    public void run(String... args) throws Exception {
        Thread.sleep(1000);
        log.info("xrpc正在启动~");
        XrpcBootstrap.getInstance()
                .scan("com.meiya.service.impl")
                .start();

    }
}
