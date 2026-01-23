package com.luguosong.ssiach12ex2.service;


import com.luguosong.ssiach12ex2.model.Product;
import org.springframework.security.access.prepost.PreFilter;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductService {

    @PreFilter("filterObject.owner == authentication.name")
    public List<Product> sellProducts(List<Product> products) {
        // 订购商品并返回所订购的商品列表
        return products;
    }
}
