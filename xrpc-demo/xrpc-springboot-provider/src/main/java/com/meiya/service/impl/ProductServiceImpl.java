package com.meiya.service.impl;

import com.meiya.ProductService;
import com.meiya.annotation.XrpcApi;

/**
 * @author xiaopf
 */
@XrpcApi(group = "primary1")
public class ProductServiceImpl implements ProductService {
    @Override
    public String getProduct() {
        return "productName!";
    }
}
