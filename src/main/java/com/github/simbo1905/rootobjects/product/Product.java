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

    @Column(name = "SKU", unique=true)
    private String sku = "";

    @Column(name = "DESCRIPTION")
    private String description = "";

    @Embedded
    private Money price = new Money("USD", new BigDecimal(0));

    public Money getPrice() {
        return price;
    }

    Product() {}

    public Product(String sku, String description, Money price) {
        this.sku = sku;
        this.description = description;
        this.price = price;
    }

    public String getSku() {
        return sku;
    }

    public Long getId() {
        return id;
    }

    public String getDescription() {
        return description;
    }
}
