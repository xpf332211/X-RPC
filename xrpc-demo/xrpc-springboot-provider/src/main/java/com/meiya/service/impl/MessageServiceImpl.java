package com.meiya.service.impl;

import com.meiya.MessageService;
import com.meiya.annotation.XrpcApi;

/**
 * @author xiaopf
 */
@XrpcApi
public class MessageServiceImpl implements MessageService {
    @Override
    public String getMessage(String name) {
        return "I get your name:" + name;
    }
}
