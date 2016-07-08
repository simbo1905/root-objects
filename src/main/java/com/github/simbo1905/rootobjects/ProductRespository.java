package com.github.simbo1905.rootobjects;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

/**
 * This is not a public class deliberately as we want a service class to save things in the correct order in a transaction.
 */
interface ProductRespository extends JpaRepository<Product, Long> {
    @Query("from Product where name = ?1")
    Product findByName(String name);
}
