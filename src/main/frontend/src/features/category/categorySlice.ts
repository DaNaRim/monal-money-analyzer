import { createSlice, type PayloadAction } from "@reduxjs/toolkit";
import { type RootState } from "../../app/store";
import { clearAuthState } from "../auth/authSlice";

export enum CategoryType {
    INCOME = "INCOME",
    OUTCOME = "OUTCOME"
}

export interface Category {
    id: number;
    name: string;
    type: CategoryType;
    subCategories: Category[];
}

interface CategoryState {
    categories: Category[];
    isInitialized: boolean;
}

const initialState: CategoryState = {
    categories: [],
    isInitialized: false,
};

const categorySlice = createSlice({
    name: "category",
    initialState,
    reducers: {
        setCategories(state, action: PayloadAction<Category[]>) {
            const categories = action.payload;

            if (categories == null || categories.length === 0) {
                return;
            }
            state.categories = action.payload;
            state.isInitialized = true;
        },
    },
    extraReducers: builder => builder.addCase(clearAuthState, () => initialState),
});

export const selectTransactionCategories = (state: RootState) => state.categories.categories;
export const selectIsCategoriesInitialized = (state: RootState) => state.categories.isInitialized;

export const selectCategoriesWithSubcategories = (state: RootState) => {
    const categories = state.categories.categories;
    const categoriesWithSubcategories = [...categories];
    categories.forEach(category => {
        if (category.subCategories != null) {
            category.subCategories.forEach(subcategory => {
                categoriesWithSubcategories.push(subcategory);
            });
        }
    });
    return categoriesWithSubcategories;
};

export const { setCategories } = categorySlice.actions;

export default categorySlice.reducer;
