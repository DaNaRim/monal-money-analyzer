import { createSlice, type PayloadAction } from "@reduxjs/toolkit";
import { type RootState } from "../../app/store";
import { clearAuthState } from "../auth/authSlice";
import { categoriesToFlatArray } from "./categoryUtil";

export enum CategoryType {
    INCOME = "INCOME",
    OUTCOME = "OUTCOME"
}

export interface Category {
    id: number;
    name: string;
    type: CategoryType;
    subCategories: Category[] | null;
}

export interface CategoryState {
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
            state.isInitialized = true;

            if (categories == null || categories.length === 0) {
                return;
            }
            state.categories = action.payload;
        },
    },
    extraReducers: builder => builder.addCase(clearAuthState, () => initialState),
});

export const selectTransactionCategories = (state: RootState) => state.categories.categories;
export const selectIsCategoriesInitialized = (state: RootState) => state.categories.isInitialized;

export const selectCategoriesWithSubcategories = (state: RootState) =>
    categoriesToFlatArray(state.categories.categories);

export const { setCategories } = categorySlice.actions;

export default categorySlice.reducer;
