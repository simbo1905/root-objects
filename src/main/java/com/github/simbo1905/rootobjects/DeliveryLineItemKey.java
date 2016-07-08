package com.github.simbo1905.rootobjects;


import javax.persistence.*;
import java.io.Serializable;

/**
 * Regrettibly to model a compound primary key in JPA we need an Embeddable object.
 * If we wnat join table of just a compound primary key then we end up with an Entity
 * which just contains an Embeddable which is two classes not one. This is the primary
 * key embeddable "value object".
 */
@Embeddable
@Access(AccessType.FIELD)
class DeliveryLineItemKey implements Serializable {

    @Column(name="DELIVERY_ID")
    private Long deliveryId;

    @Column(name="LINEITEM_ID")
    private Long lineItemId;

    DeliveryLineItemKey(){}

    public DeliveryLineItemKey(Long deliveryId, Long lineItemId){
        this.deliveryId = deliveryId;
        this.lineItemId = lineItemId;
    }

    @Transient
    Long getDeliveryId() {
        return deliveryId;
    }

    @Transient
    Long getLineItemId() {
        return lineItemId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DeliveryLineItemKey that = (DeliveryLineItemKey) o;

        if (deliveryId != null ? !deliveryId.equals(that.deliveryId) : that.deliveryId != null) return false;
        return lineItemId != null ? lineItemId.equals(that.lineItemId) : that.lineItemId == null;

    }

    @Override
    public int hashCode() {
        int result = deliveryId != null ? deliveryId.hashCode() : 0;
        result = 31 * result + (lineItemId != null ? lineItemId.hashCode() : 0);
        return result;
    }
}
