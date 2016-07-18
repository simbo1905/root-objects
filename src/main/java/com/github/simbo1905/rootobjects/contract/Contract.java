package com.github.simbo1905.rootobjects.contract;

import com.github.simbo1905.rootobjects.Money;
import com.github.simbo1905.rootobjects.product.Product;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.*;

/**
 * Contract is our main aggregate entity. It manages the Deliveries and LineItems in a delivery and ensures that a
 * line item is in one and only one delivery. It also hides as an implementation detail that there is a join table
 * entity that maps deliveries into line items. It wraps its data structures in unmodifiable copies to prevent external
 * code from corrupting things. We also avoid making methods public on other classes so that they cannot be corrupted
 * by external code. The desired net result is that you have to call public methods on the contract to alter the state
 * of anything in the contract.
 */
@Entity
@Table(name = "CONTRACT")
public class Contract {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "CONTRACT_ID", nullable=false, updatable=false)
    private Long contractId;

    @Column(name = "NAME")
    private String name = "";

    @Embedded
    private Money totalCost = new Money("USD", new BigDecimal("0.00"));

    public Money getTotalCost() {
        return totalCost;
    }

    Contract(String name) {
        this.name = name;
    }

    Contract(){}

    public String getName() {
        return name;
    }

    /**
     * This isn't public as you would probably want to use some human readable string "contract number" with an index
     * on it as the human searchable key. Sticking with package protected assigned longs make it easier to not have to
     * fight JPA.
     * @return
     */
    Long getContractId() {
        return contractId;
    }

    /**
     * This defines a foreign key relationship from deliveries back to contract.
     * Delete a delivery from this list and it will be deleted from the database due to "orphanRemoval=true".
     */
    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval=true, fetch = FetchType.LAZY)
    private List<Delivery> deliveries = new ArrayList<>();

    /**
     * Creates a delivery. Note that a delivery has no public setters so to the outside world it is unmodifiable.
     * so you can read from it but you have to call methods on this class to either add or remove line items from a
     * delivery.
     */
    public Delivery createDelivery(Date date, String location) {
        final Delivery delivery = new Delivery(this, date, location);
        deliveries.add(delivery);
        return delivery;
    }

    /**
     * This defines a foreign key relationship from line items back to contract.
     * Delete a line items from this list and it will be deleted from the database due to "orphanRemoval=true".
     */
    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval=true, fetch = FetchType.LAZY)
    private List<LineItem> lineItems = new ArrayList<>();

    /**
     * Creates a line item. Note that a line item has no public modifiers so to the outside world it is unmodifiable.
     * Updates the total cost of the contract.
     */
    public LineItem createLineItem(Product product, int quanity) {
        if( quanity < 0 ) throw new IllegalArgumentException(""+quanity);
        //We probably shouldn't allow two line items for the same product we should sum their quantities into one item.
        final LineItem lineItem = new LineItem(this, product, quanity);
        this.lineItems.add(lineItem);
        this.totalCost = this.totalCost.add(lineItem.cost());
        return lineItem;
    }

    /**
     * Deletes a line item. Removes it from the delivery it was in (if any). Deletes any join table entities that
     * associates the line item to a delivery.
     * Updates the total cost of the contract.
     */
    public boolean deleteLineItem(LineItem lineItem) {
        boolean removedFromContract = this.lineItems.remove(lineItem);
        if( removedFromContract) {
            // if the line item is already in a delivery remove it from in-memory and db join table
            if( lineItem.delivery.isPresent() ) {
                final Delivery oldDelivery = lineItem.delivery.get();
                removeLineItemFromDelivery(lineItem, oldDelivery);
            }
            // update the total cost
            this.totalCost = this.totalCost.subtract(lineItem.cost());
        }
        return removedFromContract;
    }

    public boolean updateQuanity(LineItem lineItem, int quanity) {
        if( quanity < 0 ) throw new IllegalArgumentException(""+quanity);
        boolean contains = this.lineItems.contains(lineItem);
        if( contains ) {
            lineItem.updateQuantity(quanity);
            return true;
        } else {
            return false;
        }
    }

    /**
     * This returns an unmodifiable list so that code outside of the contract cannot corrupt the state of the contract.
     */
    public List<Delivery> getDeliveries() {
        return Collections.unmodifiableList(this.deliveries);
    }

    /**
     * This returns an unmodifiable list so that code outside of the contract cannot corrupt the state of the contract.
     */
    public List<LineItem> getLineItems()  {
        return Collections.unmodifiableList(this.lineItems);
    }

    /**
     * This defines a foreign key relationsip to join table entity that stores the association of line items to delivery.
     * Delete a line items from this list and it will be deleted from the database due to "orphanRemoval=true".
     */
    @OneToMany(mappedBy = "contract", cascade = CascadeType.ALL, orphanRemoval = true, fetch =  FetchType.LAZY)
    private List<DeliveryLineItem> deliveryLineItems = new ArrayList<>();

    /**
     * This method is not public as its is only used by test code. It returns an unmodifiable list so that code in the
     * same package cannot corrupt the state of a contract.
     */
    List<DeliveryLineItem> getDeliveryLineItems() {return Collections.unmodifiableList(this.deliveryLineItems);}

    /**
     * Adds a line item into a delivery removing it from the previous delivery it was in (if any).
     * Note that the method maintains both the in-memory object layout and also maintains the join table entity
     * so that the relationship is saved to the db in a relational format.
     */
    public void addLineItemToDelivery(LineItem lineItem, Delivery delivery) {
        // if the line item is already in a delivery remove it from in-memory and db join table
        if( lineItem.delivery.isPresent() ) {
            final Delivery oldDelivery = lineItem.delivery.get();
            removeLineItemFromDelivery(lineItem, oldDelivery);
        }
        // add a join table entity for the database
        final DeliveryLineItem deliveryLineItem = new DeliveryLineItem(this, delivery, lineItem);
        this.deliveryLineItems.add(deliveryLineItem);
        // link the two object in memory
        lineItem.delivery =
                Optional.of(delivery);
        delivery.addLineItem(lineItem);
    }

    /**
     * Deletes a whole delivery. Moves any line items in a delivery out of it. Deletes any join table entities that
     * associates line items to the delivery.
     */
    public boolean deleteDelivery(Delivery delivery) {
        boolean removedFromContract = this.deliveries.remove(delivery);
        if( removedFromContract ) {
            // remove the join table entry so that the association is deleted in the database.
            delivery.getLineItems().forEach(l -> removeLineItemFromDelivery(l, delivery));
        }
        return removedFromContract;
    }

    /**
     * Removes a line item from a delivery deleting the join table entity (if any).
     */
    public boolean removeLineItemFromDelivery(LineItem lineItem, Delivery delivery) {
        // remove it from in-memory
        delivery.removeLineItem(lineItem);
        // remove the join table entry from the database
        Optional<DeliveryLineItem> optionalDeliveryLineItem =
                this.deliveryLineItems.stream().filter(
                        d -> d.getDelivery() == delivery && d.getLineItem() == lineItem).findFirst();
        if( optionalDeliveryLineItem.isPresent() ) {
            return this.deliveryLineItems.remove(optionalDeliveryLineItem.get());
        } else {
            return false;
        }
    }

    /**
     * This method is called post loading a contract from the database. It users the join table entity to know
     * which line items are in which contract and updates the objects in-memory so that the deliveries have a
     * list of their line items and a line items has a reference to its delivery (if any).
     */
    @PostLoad
    public void updateInMemoryObjectsAsPerJoinTableEntitiesInDb() {
        deliveryLineItems.forEach(dli -> {
            // ensure the delivery has this line item in its list
            dli.getDelivery().addLineItem(dli.getLineItem());
            // ensure that the line item as a reference to its delivery
            dli.getLineItem().delivery = Optional.of(dli.getDelivery());
        });
    }
}
