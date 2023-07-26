package com.danarim.monal.money.persistence.model;

import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

/**
 * Represents a category for a transaction.
 *
 * <p>Constraint "transaction_category_name_parent_category_id_uindex" generates in
 * data-categories.sql
 */
@Entity
public class TransactionCategory implements Serializable {

    @Serial
    private static final long serialVersionUID = 6199983732377986065L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column(nullable = false, updatable = false)
    private String name;

    @Column(
            columnDefinition = "VARCHAR CHECK (type IN ('INCOME', 'OUTCOME'))",
            nullable = false,
            updatable = false
    )
    @Enumerated(EnumType.STRING)
    private TransactionType type;

    @ManyToOne(targetEntity = TransactionCategory.class)
    @JoinColumn(name = "parent_category_id", updatable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private TransactionCategory parentCategory;

    /**
     * The sub-categories of the category. Sub-categories can't have sub-categories.
     */
    @Transient
    private List<TransactionCategory> subCategories;

    protected TransactionCategory() {
    }

    /**
     * Creates a new category.
     *
     * @param name           The name of the category.
     * @param type           The type of the category.
     * @param parentCategory The parent category of the category.
     */
    public TransactionCategory(String name,
                               TransactionType type,
                               TransactionCategory parentCategory
    ) {
        this.name = name;
        this.type = type;
        this.parentCategory = parentCategory;
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

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public TransactionCategory getParentCategory() {
        return parentCategory;
    }

    public void setParentCategory(TransactionCategory parentCategory) {
        this.parentCategory = parentCategory;
    }

    public List<TransactionCategory> getSubCategories() {
        return subCategories == null ? new ArrayList<>() : subCategories.stream().toList();
    }

    /**
     * Changes the sub-categories of the category. Sub-categories can't have sub-categories. Note
     * that the list is copied.
     *
     * @param subCategories The sub-categories of the category.
     */
    public void setSubCategories(List<TransactionCategory> subCategories) {
        this.subCategories = subCategories == null
                ? new ArrayList<>()
                : subCategories.stream().toList();
    }

}
