package com.meiya.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xiaopengfei
 */
@RestController
@RequestMapping("/message")
@Slf4j
public class MessageController {
    @GetMapping
    public String getMessage(){
        String name = "jerry!";
        return name;
    }
}
