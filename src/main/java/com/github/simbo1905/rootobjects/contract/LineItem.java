package com.github.simbo1905.rootobjects.contract;

import com.github.simbo1905.rootobjects.Money;
import com.github.simbo1905.rootobjects.product.Product;

import javax.persistence.*;
import java.util.Optional;

@Entity
@Table(name = "LINEITEM")
class LineItem {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "LINEITEM_ID", nullable=false, updatable=false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "PRODUCT_ID")
    private Product product;

    @Column(name = "QUANTITY")
    private Integer quantity;

    LineItem(){}

    LineItem(Contract contract, Product product, Integer quantity) {
        this.contract = contract;
        this.product = product;
        this.quantity = quantity;
    }

    Long lineItemId() {
        return id;
    }

    Product getProduct() {
        return product;
    }

    LineItem addQuantity(int quantity) {
        this.quantity = this.quantity + quantity;
        return this;
    }

    @ManyToOne
    @JoinColumn(name="CONTRACT_ID", updatable = false)
    private Contract contract;

    public Contract getContract() {
        return contract;
    }

    @Transient
    Optional<Delivery> delivery = Optional.empty();

    public Money cost() {
        return this.getProduct().getPrice().times(quantity);
    }

    /**
     * This isn't public as we must update the total cost of a contract when we change the quantity in a line item.
     * So this method is only called from within the aggregate root contract object.
     */
    void updateQuantity(int newQuantity) {
        this.quantity = newQuantity;
    }
}
