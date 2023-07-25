package com.meiya.impl;

import com.meiya.MessageService;

/**
 * @author xiaopf
 */
public class MessageServiceImpl implements MessageService {
    @Override
    public String getMessage(String name) {
        return "I get your name:" + name;
    }
}
