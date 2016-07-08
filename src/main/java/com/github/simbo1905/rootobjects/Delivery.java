package com.github.simbo1905.rootobjects;

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
    @Column(name = "DELIVERY_ID")
    private Long deliveryId;

    @Column(name = "DATE")
    private Date date;

    @Column(name = "LOCATION")
    private String location;

    Delivery(){}

    Delivery(Date date, String location){
        this.date = date;
        this.location = location;
    }

    @Transient
    private List<LineItem> lineItems = new ArrayList<>();

    public Date getDate() {
        return date;
    }

    public String getLocation() {
        return location;
    }

    public List<LineItem> getLineItems() {
        return Collections.unmodifiableList(lineItems);
    }

    public void addLineItem(LineItem lineItem) {
        this.lineItems.add(lineItem);
    }
}
