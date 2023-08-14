package com.meiya.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xiaopengfei
 */
@RestController
@RequestMapping("/test")
public class TestController {
    @GetMapping
    public String get(){
        return "find";
    }
}
