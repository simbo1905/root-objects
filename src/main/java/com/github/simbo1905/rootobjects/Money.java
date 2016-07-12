package com.github.simbo1905.rootobjects;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.math.BigDecimal;

/**
 * Don't write yoru own money class use a good opensource library!
 * This is an Embeedable meaning its a value object that we can embed with an entity or store in a collection within an
 * entity
 */
@Embeddable
public class Money {
    @Column(name = "CURRENCY", length = 3)
    private String currency;

    @Column(name = "AMOUNT")
    private BigDecimal amount;

    Money(){};

    public Money(String currency, BigDecimal amount) {
        assert currency.length() == 3;
        this.currency = currency;
        this.amount = amount;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Money add(Money other) {
        if( this.currency != other.currency )
            throw new IllegalArgumentException(String.format("%s != %s", this.currency, other.currency));
        return new Money(this.currency, this.amount.add(other.amount));
    }
    public Money subtract(Money other) {
        if( this.currency != other.currency )
            throw new IllegalArgumentException(String.format("%s != %s", this.currency, other.currency));
        return new Money(this.currency, this.amount.subtract(other.amount));
    }

    public Money times(int quanity) {
        return new Money(this.currency, this.amount.multiply(new BigDecimal(quanity)));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Money money = (Money) o;

        if (currency != null ? !currency.equals(money.currency) : money.currency != null) return false;
        return amount != null ? amount.equals(money.amount) : money.amount == null;

    }

    @Override
    public int hashCode() {
        int result = currency != null ? currency.hashCode() : 0;
        result = 31 * result + (amount != null ? amount.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Money{" +
                "currency='" + currency + '\'' +
                ", amount=" + amount +
                '}';
    }
}
