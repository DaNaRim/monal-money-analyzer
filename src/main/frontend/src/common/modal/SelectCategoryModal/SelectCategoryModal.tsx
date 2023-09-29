import { Box, Fade, Modal, Tab, Tabs } from "@mui/material";
import React, { type Dispatch, type SetStateAction, useState } from "react";
import { useAppSelector } from "../../../app/hooks/reduxHooks";
import useTranslation from "../../../app/hooks/translation";
import {
    type Category,
    CategoryType,
    selectTransactionCategories,
} from "../../../features/category/categorySlice";
import CategoryGroup from "./CategoryGroup";
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
                             label={t.data.transactionCategoryType.outcome}
                             value={CategoryType.OUTCOME}/>
                        <Tab className={styles.category_type_tab}
                             label={t.data.transactionCategoryType.income}
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
