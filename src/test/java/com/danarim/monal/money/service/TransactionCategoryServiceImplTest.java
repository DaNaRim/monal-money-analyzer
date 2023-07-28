package com.danarim.monal.money.service;

import com.danarim.monal.money.persistence.dao.TransactionCategoryDao;
import com.danarim.monal.money.persistence.model.TransactionCategory;
import com.danarim.monal.money.persistence.model.TransactionType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransactionCategoryServiceImplTest {

    private static List<TransactionCategory> daoCategories;
    private final TransactionCategoryDao categoryDao = mock(TransactionCategoryDao.class);
    @InjectMocks
    private TransactionCategoryServiceImpl categoryService;

    static {
        setupDaoCategories();
    }

    @BeforeEach
    void setUp() {
        when(categoryDao.findAll()).thenReturn(daoCategories);
    }

    @Test
    void getAvailableCategories() {
        List<TransactionCategory> categories = categoryService.getAvailableCategories();

        TransactionCategory parent1 = categories.get(0);
        TransactionCategory parent2 = categories.get(1);

        assertEquals(4, categories.size());
        assertEquals("Parent 1", parent1.getName());
        assertEquals("Parent 2", parent2.getName());
        assertEquals("Sub 1", parent1.getSubCategories().get(0).getName());
        assertEquals("Sub 2", parent1.getSubCategories().get(1).getName());
        assertEquals("Sub 3", parent2.getSubCategories().get(0).getName());
        assertEquals("Sub 4", parent2.getSubCategories().get(1).getName());
        assertEquals("Ind 1", categories.get(2).getName());
        assertEquals("Ind 2", categories.get(3).getName());
    }

    private static void setupDaoCategories() {
        TransactionCategory parent1
                = new TransactionCategory("Parent 1", TransactionType.OUTCOME, null);
        TransactionCategory parent2
                = new TransactionCategory("Parent 2", TransactionType.OUTCOME, null);

        parent1.setId(1);
        parent2.setId(2);

        daoCategories = List.of(
                parent1,
                new TransactionCategory("Sub 1", TransactionType.OUTCOME, parent1),
                new TransactionCategory("Sub 2", TransactionType.OUTCOME, parent1),
                parent2,
                new TransactionCategory("Sub 3", TransactionType.OUTCOME, parent2),
                new TransactionCategory("Sub 4", TransactionType.OUTCOME, parent2),
                new TransactionCategory("Ind 1", TransactionType.OUTCOME, null),
                new TransactionCategory("Ind 2", TransactionType.INCOME, null)
        );
    }

}
