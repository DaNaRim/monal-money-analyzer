package com.danarim.monal.money.web.dto;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.util.Date;

/**
 * DTO for viewing a {@link com.danarim.monal.money.persistence.model.Transaction Transaction}.
 */
public class ViewTransactionDto {

    private long id;

    private String description;

    @JsonFormat(
            shape = JsonFormat.Shape.NUMBER,
            without = JsonFormat.Feature.WRITE_DATE_TIMESTAMPS_AS_NANOSECONDS
    )
    private Date date;

    private double amount;

    private long categoryId;

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

}
