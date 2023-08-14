package com.meiya.controller;

import com.meiya.MessageService;
import com.meiya.annotation.XrpcService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xiaopengfei
 */
@RestController
@RequestMapping("/message")
@Slf4j
public class MessageController {

    @XrpcService
    private MessageService messageService;

    @RequestMapping
    public String getMassage() {
        return messageService.getMessage("gg-bond");
    }
}
