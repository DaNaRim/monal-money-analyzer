package com.danarim.monal.money.persistence.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * Represents a financial transaction.
 */
@Entity
public class Transaction implements Serializable {

    @Serial
    private static final long serialVersionUID = 1479527315850134208L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String description; // Can be empty

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    /**
     * The amount of the transaction. Can't be negative or zero.
     */
    @Column(
            columnDefinition = "NUMERIC(1000, 8) CHECK (amount > 0)",
            nullable = false
    )
    private double amount;

    @ManyToOne(targetEntity = TransactionCategory.class)
    @JoinColumn(name = "category_id", nullable = false)
    private TransactionCategory category;

    @ManyToOne(targetEntity = Wallet.class)
    @JoinColumn(name = "wallet_id", nullable = false)
    private Wallet wallet;

    protected Transaction() {
    }

    /**
     * Creates a new transaction for the given wallet.
     *
     * @param description A description of the transaction.
     * @param date        The date of the transaction.
     * @param amount      The amount of the transaction. Can't be negative or zero.
     * @param category    The category of the transaction.
     * @param wallet      The wallet the transaction belongs to.
     */
    public Transaction(String description,
                       Date date,
                       double amount,
                       TransactionCategory category,
                       Wallet wallet
    ) {
        this.description = description;
        this.date = new Date(date.getTime());
        this.amount = amount;
        this.category = category;
        this.wallet = wallet;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getDate() {
        return new Date(date.getTime());
    }

    public void setDate(Date date) {
        this.date = new Date(date.getTime());
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public TransactionCategory getCategory() {
        return category;
    }

    public void setCategory(TransactionCategory category) {
        this.category = category;
    }

    public Wallet getWallet() {
        return wallet;
    }

    public void setWallet(Wallet wallet) {
        this.wallet = wallet;
    }

}
