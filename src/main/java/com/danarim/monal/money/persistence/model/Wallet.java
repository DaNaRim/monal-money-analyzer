package com.danarim.monal.money.persistence.model;

import com.danarim.monal.user.persistence.model.User;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.io.Serial;
import java.io.Serializable;
import java.util.Currency;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;

/**
 * Represents a wallet that belongs to a user.
 */
@Entity
public class Wallet implements Serializable {

    @Serial
    private static final long serialVersionUID = -7394214343225810576L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false)
    private String name;

    @Column(scale = 2, nullable = false)
    private double balance;

    @Column(nullable = false)
    private String currency; // In database, currency is stored as a String

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false, updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User owner;

    protected Wallet() {
    }

    /**
     * Creates a new wallet.
     *
     * @param name     The name of the wallet.
     * @param balance  Initial balance of the wallet. Can be negative.
     * @param currency The currency of the wallet.
     * @param owner    The owner of the wallet.
     */
    public Wallet(String name, double balance, String currency, User owner) {
        this.name = name;
        this.balance = balance;
        this.currency = currency;
        this.owner = owner;
    }


    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public Currency getCurrency() {
        return Currency.getInstance(currency); //conversion to an actual currency
    }

    public void setCurrency(Currency currency) {
        this.currency = currency.getCurrencyCode(); //conversion to an actual currency
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

}
