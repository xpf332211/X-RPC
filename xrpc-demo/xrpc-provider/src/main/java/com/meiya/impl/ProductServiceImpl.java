package com.meiya.impl;

import com.meiya.ProductService;

/**
 * @author xiaopf
 */
public class ProductServiceImpl implements ProductService {
    @Override
    public String getProduct() {
        return "productName!";
    }
}
