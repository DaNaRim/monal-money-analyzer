package com.danarim.monal.money.web.controller;

import com.danarim.monal.config.WebConfig;
import com.danarim.monal.failhandler.RestExceptionHandler;
import com.danarim.monal.money.persistence.model.TransactionCategory;
import com.danarim.monal.money.persistence.model.TransactionType;
import com.danarim.monal.money.service.TransactionCategoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static com.danarim.monal.TestUtils.getExt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TransactionCategoryController.class)
@ContextConfiguration(classes = {TransactionCategoryController.class, RestExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class TransactionCategoryControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TransactionCategoryService categoryService;

    @Test
    void getAvailableCategories() throws Exception {
        TransactionCategory parentCategory =
                new TransactionCategory("TestParent", TransactionType.OUTCOME, null);

        parentCategory.setSubCategories(List.of(
                new TransactionCategory("Child", TransactionType.OUTCOME, parentCategory)
        ));
        List<TransactionCategory> categories = List.of(
                parentCategory,
                new TransactionCategory("Test3", TransactionType.INCOME, null)
        );

        when(categoryService.getAvailableCategories()).thenReturn(categories);

        mockMvc.perform(getExt(WebConfig.API_V1_PREFIX + "/category"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value(parentCategory.getName()))
                .andExpect(jsonPath("$[0].type").value(parentCategory.getType().toString()))
                .andExpect(jsonPath("$[0].parent").doesNotExist())
                .andExpect(jsonPath("$[0].subCategories[0].name")
                                   .value(parentCategory.getSubCategories().get(0).getName()))
                .andExpect(jsonPath("$[0].subCategories[0].type")
                                   .value(parentCategory.getSubCategories().get(0).getType()
                                                  .toString()))
                .andExpect(jsonPath("$[0].subCategories[0].parent").doesNotExist())
                .andExpect(jsonPath("$[0].subCategories[0].subCategories").doesNotExist())

                .andExpect(jsonPath("$[1].name").value(categories.get(1).getName()))
                .andExpect(jsonPath("$[1].type").value(categories.get(1).getType().toString()))
                .andExpect(jsonPath("$[1].parent").doesNotExist())
                // If there are no sub categories, the subCategories field should not exist.
                .andExpect(jsonPath("$[1].subCategories").doesNotExist());
    }

}
