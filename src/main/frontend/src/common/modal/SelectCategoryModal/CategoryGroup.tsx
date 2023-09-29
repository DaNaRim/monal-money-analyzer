import React, { useState } from "react";
import { useAppSelector } from "../../../app/hooks/reduxHooks";
import useTranslation from "../../../app/hooks/translation";
import {
    type Category,
    selectTransactionCategories,
} from "../../../features/category/categorySlice";
import { getCategoryLocalName } from "../../../features/category/categoryUtil";
import styles from "./SelectCategoryModal.module.scss";

interface CategoryGroupProps {
    category: Category;
    selectedCategory?: Category;
    onChoose: (category: Category) => void;
}

const CategoryGroup = ({ category, onChoose, selectedCategory }: CategoryGroupProps) => {
    const t = useTranslation();

    const [isExpanded, setIsExpanded] = useState(false);

    const subCategories = category.subCategories;

    const categories = useAppSelector(selectTransactionCategories);

    const getSubCategory = (subCategory: Category) => (
        <div className={`${styles.subcategories_dot_wrapper}`
            + `${selectedCategory?.id === subCategory.id ? styles.selected : ""}`}
             onClick={() => onChoose(subCategory)}
             key={subCategory.id}
             role="option">
            <div className={styles.subcategories_dot}></div>
            <div className={styles.subcategory}>
                {getCategoryLocalName(subCategory, categories, t)}
            </div>
        </div>
    );

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
            <div className={`${styles.parent_option}`
                + ` ${selectedCategory?.id === category.id ? styles.selected : ""}`}
                 onClick={() => onChoose(category)}
                 role="option">
                {getCategoryLocalName(category, categories, t)}
            </div>
            {getSubcategories()}
        </div>
    );
};

export default CategoryGroup;
