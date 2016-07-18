package com.github.simbo1905.rootobjects.product;

import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;

@Named("productService")
public class ProductService {
    @Inject ProductRespository productRepository;

    @Transactional
    public void save(Product product) {
        productRepository.save(product);
    }

    @Transactional
    public Product findByName(String name) {
        return productRepository.findByName(name);
    }
}
