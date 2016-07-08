package com.github.simbo1905.rootobjects;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "CONTRACT")
class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "CONTRACT_ID")
    private Long contractId;

    @Column(name = "NAME")
    private String name = "";

    Contract(String name) {
        this.name = name;
    }

    Contract(){}

    String getName() {
        return name;
    }

    Long getContractId() {
        return contractId;
    }

    @OneToMany(cascade = CascadeType.ALL)
    private List<Delivery> deliveries = new ArrayList<>();

    public Delivery createDelivery(Date date, String location) {
        final Delivery delivery = new Delivery(date, location);
        deliveries.add(delivery);
        return delivery;
    }

    public List<Delivery> getDeliveries() {
        return Collections.unmodifiableList(this.deliveries);
    }

    @OneToMany(cascade = CascadeType.ALL)
    private List<LineItem> lineItems = new ArrayList<>();

    public List<LineItem> getLineItems() {
        return Collections.unmodifiableList(this.lineItems);
    }

    public void addProductToDelivery(Product product, final String deliveryLocation, int quantity) {

        // find the delivery by deliveryLocation
        final Optional<Delivery> deliveryOptional =
                deliveries.stream().filter(s -> s.getLocation() == deliveryLocation).findFirst();

        // if it doesn't exist throw an exception
        if( !deliveryOptional.isPresent() )
            throw new IllegalArgumentException(String.format("no delivery for location %s", deliveryLocation));

        final Delivery delivery = deliveryOptional.get();

        // find a line item in the delivery for the produst if it exists
        final Optional<LineItem> lineItemOptional =
                delivery.getLineItems().stream().filter(s -> s.getProduct().getName() == product.getName()).findFirst();

        // if we had a line item increment its quantity, it not create a new one
        final LineItem lineItem =
                lineItemOptional.isPresent() ? lineItemOptional.get().addQuantity(quantity) : new LineItem(product, quantity);

        // if the line item isn't yet in the delivery add it into it the delivery
        if( !lineItemOptional.isPresent() ){
            this.lineItems.add(lineItem);
            delivery.addLineItem(lineItem);
        }

    }


}
