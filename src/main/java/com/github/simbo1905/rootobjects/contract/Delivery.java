package com.github.simbo1905.rootobjects.contract;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "DELIVERY")
class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "DELIVERY_ID", nullable=false, updatable=false)
    private Long deliveryId;

    @Column(name = "DATE")
    private Date date;

    @Column(name = "LOCATION")
    private String location;

    Delivery(){}

    Delivery(Contract contract, Date date, String location){
        this.contract = contract;
        this.date = date;
        this.location = location;
    }

    @Transient
    List<LineItem> lineItems = new ArrayList<>();

    public List<LineItem> getLineItems() {
        return Collections.unmodifiableList(lineItems);
    }

    public Date getDate() {
        return date;
    }

    public String getLocation() {
        return location;
    }

    public Long deliveryId() {
        return this.deliveryId;
    }

    @ManyToOne
    @JoinColumn(name="CONTRACT_ID", updatable = false)
    private Contract contract;

    public Contract getContract() {
        return contract;
    }

    /**
     * This method isn't public as contract will maintain a join table for this relationship so requests to
     * add or remove line items must be via the appropriate contract public methods.
     */
    boolean addLineItem(LineItem lineItem) {
        return lineItems.add(lineItem);
    }

    /**
     * This method isn't public as contract will maintain a join table for this relationship so requests to
     * add or remove line items must be via the appropriate contract public methods.
     */
    boolean removeLineItem(LineItem lineItem) {
        return lineItems.remove(lineItem);
    }
}
