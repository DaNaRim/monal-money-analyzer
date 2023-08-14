import { createSlice } from "@reduxjs/toolkit";
import { type TransactionType } from "../transaction/transactionSlice";

export interface Category {
    id: number;
    name: string;
    type: TransactionType;
    subCategories: Category[];
};

interface CategoryState {
    categories: Category[];
};

const initialState: CategoryState = {
    categories: [],
};

const categorySlice = createSlice({
    name: "category",
    initialState,
    reducers: {
        setCategories(state, action) {
            state.categories = action.payload;
        },
    },
});

export const selectTransactionCategories = (state: CategoryState) => state.categories;

export const { setCategories } = categorySlice.actions;

export default categorySlice.reducer;
