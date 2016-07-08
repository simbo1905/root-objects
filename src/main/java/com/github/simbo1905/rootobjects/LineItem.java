package com.github.simbo1905.rootobjects;

import javax.persistence.*;

@Entity
@Table(name = "LINEITEM")
class LineItem {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "LINEITEM_ID")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "PRODUCT_ID")
    private Product product;

    @Column(name = "QUANTITY")
    private Integer quantity;

    LineItem(){}

    LineItem(Product product, Integer quantity) {
        this.product = product;
        this.quantity = quantity;

    }

    Long getId() {
        return id;
    }

    Product getProduct() {
        return product;
    }

    public LineItem addQuantity(int quantity) {
        this.quantity = this.quantity + quantity;
        return this;
    }
}
