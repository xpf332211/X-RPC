package com.meiya;

import com.meiya.impl.MessageServiceImpl;

import com.meiya.impl.ProductServiceImpl;
import lombok.extern.slf4j.Slf4j;

import java.io.UnsupportedEncodingException;


/**
 * @author xiaopf
 */
@Slf4j
public class ProviderApplication {
    public static void main(String[] args) throws UnsupportedEncodingException {
        XrpcBootstrap.getInstance()
                .scan("com.meiya")
                .start();
    }
}
