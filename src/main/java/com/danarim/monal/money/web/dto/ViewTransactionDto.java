package com.danarim.monal.money.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;
import java.util.Objects;

/**
 * DTO for viewing a {@link com.danarim.monal.money.persistence.model.Transaction Transaction}.
 */
public class ViewTransactionDto implements Comparable<ViewTransactionDto> {

    private long id;

    private String description;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private Date date;

    private double amount;

    private long categoryId;

    private long walletId;

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

    public long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(long categoryId) {
        this.categoryId = categoryId;
    }

    public long getWalletId() {
        return walletId;
    }

    public void setWalletId(long walletId) {
        this.walletId = walletId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, description, date, amount, categoryId, walletId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ViewTransactionDto that = (ViewTransactionDto) o;
        return id == that.id
                && Double.compare(amount, that.amount) == 0
                && categoryId == that.categoryId
                && walletId == that.walletId
                && Objects.equals(description, that.description)
                && Objects.equals(date, that.date);
    }

    @Override
    public int compareTo(ViewTransactionDto o) {
        //Compare by date. Newest first.
        if (this.date.after(o.date)) {
            return -1;
        }
        if (this.date.before(o.date)) {
            return 1;
        }
        return 0;
    }

}
