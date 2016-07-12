package com.github.simbo1905.rootobjects.product;

import com.github.simbo1905.rootobjects.Money;

import javax.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "PRODUCT")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "PRODUCT_ID", nullable=false, updatable=false)
    private Long id;

    @Column(name = "NAME")
    private String name = "";

    @Embedded
    private Money price = new Money("USD", new BigDecimal(0));

    public Money getPrice() {
        return price;
    }

    Product() {}

    public Product(String name, Money price) {
        this.name = name;
        this.price = price;
    }

    public String getName() {
        return name;
    }

    public Long getId() {
        return id;
    }
}
