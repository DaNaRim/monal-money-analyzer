package com.danarim.monal.money.web.dto;

import com.danarim.monal.money.persistence.model.TransactionCategory;
import com.danarim.monal.money.persistence.model.TransactionType;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for viewing {@link TransactionCategory}.
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public class ViewTransactionCategoryDto {

    private long id;

    private String name;

    private TransactionType type;

    private List<ViewTransactionCategoryDto> subCategories;

    public ViewTransactionCategoryDto() {
        // Empty constructor for model mapper.
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

    public List<ViewTransactionCategoryDto> getSubCategories() {
        return subCategories == null ? new ArrayList<>() : subCategories.stream().toList();
    }

    /**
     * Changes the sub-categories of the category. Sub-categories can't have sub-categories. Note
     * that the list is copied.
     *
     * @param subCategories Sub categories to set.
     */
    public void setSubCategories(List<ViewTransactionCategoryDto> subCategories) {
        this.subCategories = subCategories == null
                ? new ArrayList<>()
                : subCategories.stream().toList();
    }

    /**
     * If the sub categories list is null, it will be initialized as an empty list. Then the sub
     * category will be added to the list. If the sub category is null, nothing will happen.
     *
     * @param subCategory Sub category to add.
     */
    public void addSubCategory(ViewTransactionCategoryDto subCategory) {
        if (this.subCategories == null) {
            this.subCategories = new ArrayList<>();
        }
        if (subCategory != null) {
            this.subCategories.add(subCategory);
        }
    }

}
