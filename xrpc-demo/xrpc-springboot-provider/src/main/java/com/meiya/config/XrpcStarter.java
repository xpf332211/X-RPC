package com.meiya.config;

import com.meiya.bootstrap.ScanServiceStarter;
import com.meiya.bootstrap.XrpcBootstrap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.UnsupportedEncodingException;

/**
 * @author xiaopengfei
 */
@Slf4j
@Component
public class XrpcStarter implements ApplicationRunner {
    @Override
    public void run(ApplicationArguments args) throws Exception {
        new ScanServiceStarter("com.meiya.service.impl").start();
    }
}
