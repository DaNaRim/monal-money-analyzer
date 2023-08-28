import { createSlice, type PayloadAction } from "@reduxjs/toolkit";
import dayjs from "dayjs";
import { type RootState } from "../../app/store";
import { clearAuthState } from "../auth/authSlice";
import { type Category } from "../category/categorySlice";

export interface Transaction {
    id: number;
    description: string;
    date: Date;
    amount: number;
    category: Category | null; // null if category is not found
}

// format in which date is received from server
export interface ViewTransactionDto {
    id: number;
    description: string;
    date: Date;
    amount: number;
    categoryId: number;
}

export interface CreateTransactionDto {
    description: string;
    date: Date;
    amount: number;
    categoryId: number;
    walletId: number;
}

export type TransactionsState = {
    [walletId in number]: {
        [date in string]: Transaction[]
    };
};

const initialState: TransactionsState = {};

interface SetTransactionsPayload {
    walletId: number;
    date: string;
    transactions: Transaction[];
}

interface SetTransactionPayload {
    walletId: number;
    transaction: Transaction;
}

const transactionsSlice = createSlice({
    name: "transactions",
    initialState,
    reducers: {
        saveTransactions(state, action: PayloadAction<SetTransactionsPayload>) {
            const { walletId, date, transactions } = action.payload;

            if (state[walletId] == null) {
                state[walletId] = {};
            }

            transactions.sort(sortByDate);
            state[walletId][date] = transactions;
        },
        addTransaction(state, action: PayloadAction<SetTransactionPayload>) {
            const { walletId, transaction } = action.payload;

            const date = dayjs(transaction.date).format("YYYY-MM-DD");

            if (state[walletId] == null) {
                state[walletId] = {};
            }
            if (state[walletId][date] == null) {
                state[walletId][date] = [];
            }
            state[walletId][date].push(transaction);
            state[walletId][date].sort(sortByDate);
        },
    },
    extraReducers: builder => builder.addCase(clearAuthState, () => initialState),
});

export const selectIsTransactionsInitByWalletAndDate = (state: RootState,
                                                        walletId: number,
                                                        date: string,
): boolean => state.transactions[walletId]?.[date] != null;

export const selectTransactionsByWalletAndDate = (state: RootState,
                                                  walletId: number,
                                                  date: string,
): Transaction[] => state.transactions[walletId]?.[date] ?? [];

export const {
    saveTransactions,
    addTransaction,
} = transactionsSlice.actions;

export default transactionsSlice.reducer;

function sortByDate(a: Transaction, b: Transaction) {
    if (dayjs(a.date).isAfter(dayjs(b.date))) {
        return -1;
    }
    if (dayjs(a.date).isBefore(dayjs(b.date))) {
        return 1;
    }
    return 0;
}
