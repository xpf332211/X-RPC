package com.meiya.impl;

import com.meiya.MessageService;
import com.meiya.annotation.Retry;
import com.meiya.annotation.XrpcApi;

/**
 * @author xiaopf
 */
@XrpcApi(group = "default2")
public class MessageServiceImpl implements MessageService {
    @Override
    public String getMessage(String name) {
        return "I get your name:" + name;
    }
}
