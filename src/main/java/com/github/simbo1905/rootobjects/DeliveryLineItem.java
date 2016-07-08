package com.github.simbo1905.rootobjects;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "DELIVERY_LINEITEM")
class DeliveryLineItem {

    private DeliveryLineItemKey key;

    /**
     * To model a compound primary key in JPA we need an Embeddable object.
     * If we wnat join table of just a compound primary key then we end up with an Entity
     * which just contains an Embeddable. If we needed more fields on the join table
     * then they would be modelled in this class.
     */
    @EmbeddedId
    DeliveryLineItemKey getKey() {
        return key;
    }

    void setKey(DeliveryLineItemKey key) {
        this.key = key;
    }

    DeliveryLineItem(){}

    DeliveryLineItem(Long deliveryId, Long lineItemId) {
        this.setKey(new DeliveryLineItemKey(deliveryId, lineItemId));
    }

    @Transient
    Long getDeliveryId() {
        return getKey().getDeliveryId();
    }

    @Transient
    Long getLineItemId() {
        return getKey().getLineItemId();
    }
}
