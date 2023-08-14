package com.meiya;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author xiaopengfei
 */
@SpringBootApplication
@Slf4j
public class ProviderApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProviderApplication.class,args);
        log.info("provider-springboot启动完毕~");
    }
}
