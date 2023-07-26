package com.danarim.monal.money.service;

import com.danarim.monal.money.persistence.model.TransactionCategory;

import java.util.List;

/**
 * Service for {@link TransactionCategory}.
 */
public interface TransactionCategoryService {

    List<TransactionCategory> getAvailableCategories();

}
