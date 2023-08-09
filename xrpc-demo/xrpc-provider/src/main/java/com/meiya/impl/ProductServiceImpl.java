package com.meiya.impl;

import com.meiya.ProductService;
import com.meiya.annotation.XrpcApi;

/**
 * @author xiaopf
 */
@XrpcApi
public class ProductServiceImpl implements ProductService {
    @Override
    public String getProduct() {
        return "productName!";
    }
}
