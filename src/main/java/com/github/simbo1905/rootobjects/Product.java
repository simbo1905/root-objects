package com.github.simbo1905.rootobjects;

import javax.persistence.*;

@Entity
@Table(name = "PRODUCT")
class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "PRODUCT_ID")
    private Long id;

    @Column(name = "NAME")
    private String name = "";

    Product(String name) {
        this.name = name;
    }

    String getName() {
        return name;
    }

    Long getId() {
        return id;
    }
}
