package com.meiya.controller;

import com.meiya.ProductService;
import com.meiya.annotation.XrpcService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author xiaopengfei
 */
@RestController
@RequestMapping("/product")
public class ProductController {
    @XrpcService(group = "primary2")
    private ProductService productService;
    @GetMapping
    public String getProduct(){
        return productService.getProduct();
    }
}
