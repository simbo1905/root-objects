package com.github.simbo1905.rootobjects.product;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * This is not a public class deliberately as we want a service class to save things in the correct order in a transaction.
 */
interface ProductRespository extends JpaRepository<Product, Long> {
    @Query("select p from Product p where p.sku = ?1")
    Product findBySku(String name);
}
