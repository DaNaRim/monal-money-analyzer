package com.danarim.monal.money.web.controller;

import com.danarim.monal.config.WebConfig;
import com.danarim.monal.money.persistence.model.TransactionCategory;
import com.danarim.monal.money.service.TransactionCategoryService;
import com.danarim.monal.money.web.dto.ViewTransactionCategoryDto;
import org.modelmapper.ModelMapper;
import org.modelmapper.PropertyMap;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controller for {@link TransactionCategory}.
 */
@RestController
@RequestMapping(WebConfig.API_V1_PREFIX + "/category")
public class TransactionCategoryController {

    private static final ModelMapper modelMapper = new ModelMapper();
    private static final PropertyMap<TransactionCategory, ViewTransactionCategoryDto> skipped =
            new PropertyMap<>() {
                @Override
                protected void configure() {
                    skip().setSubCategories(null); // Skip sub categories when mapping.
                }
            };

    private final TransactionCategoryService categoryService;

    /**
     * Dependency injection constructor. Also configures the {@link ModelMapper}.
     *
     * @param categoryService {@link TransactionCategoryService}.
     */
    public TransactionCategoryController(TransactionCategoryService categoryService) {
        this.categoryService = categoryService;

        // If the mapping is not already configured, configure it.
        if (modelMapper.getTypeMap(TransactionCategory.class,
                                   ViewTransactionCategoryDto.class) == null) {
            modelMapper.addMappings(skipped);
        }
    }

    /**
     * Endpoint for getting all available categories.
     *
     * @return all available categories.
     */
    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    public List<ViewTransactionCategoryDto> getAvailableCategories() {
        List<TransactionCategory> categories = categoryService.getAvailableCategories();

        return categories.stream()
                .map(category -> {
                    ViewTransactionCategoryDto result = // Map category without sub categories.
                            modelMapper.map(category, ViewTransactionCategoryDto.class);

                    category.getSubCategories().forEach(subCategory -> {
                        result.addSubCategory(// Map sub category and add it to the parent category
                                              modelMapper.map(subCategory,
                                                              ViewTransactionCategoryDto.class)
                        );
                    });
                    return result;
                })
                .toList();
    }

}
