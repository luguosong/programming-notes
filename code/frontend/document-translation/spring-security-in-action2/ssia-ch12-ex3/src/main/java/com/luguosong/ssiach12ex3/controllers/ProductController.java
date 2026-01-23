package com.luguosong.ssiach12ex3.controllers;

import com.luguosong.ssiach12ex3.model.Product;
import com.luguosong.ssiach12ex3.service.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ProductController {

	private final ProductService productService;

	public ProductController(ProductService productService) {
		this.productService = productService;
	}

	@GetMapping("/find")
	public List<Product> findProducts() {
		return productService.findProducts();
	}
}
