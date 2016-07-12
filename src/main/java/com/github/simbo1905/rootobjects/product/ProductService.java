package com.github.simbo1905.rootobjects.product;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * This class is a public service class that lets us load and save products which are root entity.
 */
@Named("productService")
public class ProductService {
    @Inject ProductRespository productRepository;

    public void save(Product product) {
        productRepository.save(product);
    }

    public Product findByName(String name) {
        return productRepository.findByName(name);
    }
}
