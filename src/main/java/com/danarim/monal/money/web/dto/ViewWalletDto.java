package com.danarim.monal.money.web.dto;

/**
 * DTO for viewing {@link com.danarim.monal.money.persistence.model.Wallet} entities.
 */
public final class ViewWalletDto {

    private long id;
    private String name;
    private double balance;
    private String currency;

    public ViewWalletDto() {
        // Empty constructor for ModelMapper.
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

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

}
