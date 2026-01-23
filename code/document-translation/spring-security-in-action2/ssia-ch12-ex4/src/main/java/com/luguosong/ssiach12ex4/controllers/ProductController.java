package com.luguosong.ssiach12ex4.controllers;


import com.luguosong.ssiach12ex4.entities.Product;
import com.luguosong.ssiach12ex4.repositories.ProductRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProductController {

    private final ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping("/products/{text}")
    public List<Product> findProductsContaining(@PathVariable String text) {
        return productRepository.findProductByNameContains(text);
    }
}
