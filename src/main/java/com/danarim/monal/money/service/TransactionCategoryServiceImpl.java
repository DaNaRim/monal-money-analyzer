package com.danarim.monal.money.service;

import com.danarim.monal.money.persistence.dao.TransactionCategoryDao;
import com.danarim.monal.money.persistence.model.TransactionCategory;
import com.danarim.monal.money.persistence.model.TransactionType;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for {@link TransactionCategory}.
 */
@Service
public class TransactionCategoryServiceImpl implements TransactionCategoryService {

    private final TransactionCategoryDao transactionCategoryDao;

    public TransactionCategoryServiceImpl(TransactionCategoryDao transactionCategoryDao) {
        this.transactionCategoryDao = transactionCategoryDao;
    }

    /**
     * Returns all parent categories with their sub categories in a tree structure.
     *
     * @return all available categories.
     */
    @Override
    public List<TransactionCategory> getAvailableCategories() {
        List<TransactionCategory> mixedCategories = transactionCategoryDao.findAll();

        List<TransactionCategory> categories = mixedCategories.stream()
                .filter(category -> category.getParentCategory() == null)
                .toList();

        categories.forEach(category -> {
            category.setSubCategories(getSubCategories(category.getId(), mixedCategories));
        });
        return categories;
    }

    @Override
    public TransactionType getCategoryType(long categoryId) {
        return transactionCategoryDao.getTypeById(categoryId);
    }

    /**
     * Searches for sub categories of the parent category in the list of all categories.
     *
     * @param parentCategoryId ID of the parent category.
     * @param mixedCategories  List of all categories.
     *
     * @return List of sub categories of the parent category.
     */
    private static List<TransactionCategory> getSubCategories(
            long parentCategoryId,
            List<TransactionCategory> mixedCategories
    ) {
        return mixedCategories.stream()
                .filter(category -> category.getParentCategory() != null
                        && category.getParentCategory().getId() == parentCategoryId)
                .toList();
    }

}
