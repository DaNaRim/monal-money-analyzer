import { Box, Fade, Modal, Tab, Tabs } from "@mui/material";
import React, { type Dispatch, type SetStateAction, useState } from "react";
import { useAppSelector } from "../../../app/hooks/reduxHooks";
import useTranslation from "../../../app/hooks/translation";
import {
    type Category,
    CategoryType,
    selectTransactionCategories,
} from "../../../features/category/categorySlice";
import styles from "./SelectCategoryModal.module.scss";

interface SelectCategoryModalProps {
    open: boolean;
    setOpen: Dispatch<SetStateAction<boolean>>;
    setCategory: Dispatch<SetStateAction<Category | undefined>>;
    selectedCategory?: Category;
}

const SelectCategoryModal = ({
                                 open,
                                 setOpen,
                                 setCategory,
                                 selectedCategory,
                             }: SelectCategoryModalProps) => {
    const t = useTranslation();

    const [selectedTab, setSelectedTab] = useState<CategoryType>(CategoryType.OUTCOME);

    const categories = useAppSelector(selectTransactionCategories);

    const handleCategoryClick = (category: Category) => {
        setCategory(category);
        setOpen(false);
    };

    return (
        <Modal
            open={open}
            onClose={() => setOpen(false)}
        >
            <Fade in={open}>
                <Box className={styles.modal_block}>
                    <Tabs value={selectedTab}
                          classes={{
                              indicator: styles.category_type_tab_indicator,

                          }}
                          onChange={(_e, value) => setSelectedTab(value)}
                          aria-label="category type">
                        <Tab className={styles.category_type_tab}
                             label={t.selectCategoryModal.outcome}
                             value={CategoryType.OUTCOME}/>
                        <Tab className={styles.category_type_tab}
                             label={t.selectCategoryModal.income}
                             value={CategoryType.INCOME}/>
                    </Tabs>
                    <div className={styles.categories_list}>
                        {categories.filter(category => category.type === selectedTab)
                            .map(category => (
                                <CategoryGroup key={category.id}
                                               selectedCategory={selectedCategory}
                                               category={category}
                                               onChoose={handleCategoryClick}/>
                            ))
                        }
                    </div>
                </Box>
            </Fade>
        </Modal>
    );
};

export default SelectCategoryModal;

interface CategoryGroupProps {
    category: Category;
    selectedCategory?: Category;
    onChoose: (category: Category) => void;
}

const CategoryGroup = ({ category, onChoose, selectedCategory }: CategoryGroupProps) => {
    const t = useTranslation();

    const [isExpanded, setIsExpanded] = useState(false);

    const subCategories = category.subCategories;

    const getSubCategory = (subCategory: Category) => (
        <div className={`${styles.subcategories_dot_wrapper}`
            + `${selectedCategory?.id === subCategory.id ? styles.selected : ""}`}
             onClick={() => onChoose(subCategory)}
             role="option">
            <div className={styles.subcategories_dot}></div>
            <div className={styles.subcategory}>
                {getCategoryLocalName(subCategory)}
            </div>
        </div>
    );

    const getCategoryLocalName = (category: Category | undefined) => {
        const categoryNameKey
            = category?.name.toLowerCase().replaceAll(" ", "_") ?? "";

        return category == null
            ? t.data.transactionCategory.deleted
            : t.getString(
                `data.transactionCategory.${category.type.toLowerCase()}.${categoryNameKey}`,
            );
    };

    const getSubcategories = () => {
        if (subCategories == null) {
            return null;
        }
        if (isExpanded || subCategories.length < 3) {
            return (
                <div className={styles.subcategories_list}>
                    <div className={styles.dotsWrapper}>
                        <div className={styles.dots}></div>
                        <div className={styles.sub_categories}>
                            {subCategories.map(subCategory => getSubCategory(subCategory))}
                        </div>
                    </div>
                    {subCategories.length > 2
                        && <button className={styles.expand_button}
                                   onClick={() => setIsExpanded(false)}>
                            {t.selectCategoryModal.showLess}
                      </button>
                    }
                </div>
            );
        }
        return (
            <div className={styles.subcategories_list}>
                <div className={styles.dotsWrapper}>
                    <div className={styles.dots}></div>
                    <div className={styles.sub_categories}>
                        {getSubCategory(subCategories[0])}
                        {getSubCategory(subCategories[1])}
                    </div>
                </div>
                <button className={styles.expand_button} onClick={() => setIsExpanded(true)}>
                    {t.selectCategoryModal.showMore}
                </button>
            </div>
        );
    };

    return (
        <div className={styles.category_group}>
            <div className={`${styles.parent_option} ${selectedCategory?.id === category.id
                ? styles.selected
                : ""}`}
                 onClick={() => onChoose(category)} role="option">
                {getCategoryLocalName(category)}
            </div>
            {getSubcategories()}
        </div>
    );
};
