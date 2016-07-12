package com.github.simbo1905.rootobjects.contract;

import javax.persistence.*;

@Entity
@Table(name = "DELIVERY_LINEITEM")
class DeliveryLineItem {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "ID", nullable=false, updatable=false)
    private Long deliveryLineItemId;

    @ManyToOne
    @JoinColumn(name="CONTRACT_ID", updatable = false)
    private Contract contract;

    @ManyToOne
    @JoinColumn(name = "DELIVERY_ID")
    private Delivery delivery;

    @ManyToOne
    @JoinColumn(name = "LINEITEM_ID")
    private LineItem lineItem;

    public DeliveryLineItem(){}

    public DeliveryLineItem(Contract contract, Delivery delivery, LineItem lineItem){
        this.contract = contract;
        this.delivery = delivery;
        this.lineItem = lineItem;
    }

//    public Long getDeliveryId() {
//        return this.delivery.deliveryId();
//    }
//
//    public Long getLineItemId() {
//        return this.lineItem.lineItemId();
//    }

    public Long getDeliveryLineItemId() {
        return deliveryLineItemId;
    }

    public Contract getContract() {
        return contract;
    }

    public LineItem getLineItem() {
        return lineItem;
    }

    public Delivery getDelivery() {
        return delivery;
    }
}
