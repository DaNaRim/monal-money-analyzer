import { createSlice, type PayloadAction } from "@reduxjs/toolkit";

export enum TransactionType {
    INCOME = "INCOME",
    OUTCOME = "OUTCOME"
}

export interface Transaction {
    id: number;
    description: string;
    date: Date;
    amount: number;
    categoryId: number;
}

type TransactionsState = {
    [walletId in number]: {
        [date in string]: Transaction[];
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
    date: string;
    transaction: Transaction;
}

const transactionsSlice = createSlice({
    name: "transactions",
    initialState,
    reducers: {
        saveTransactionsByWalletAndDate(state, action: PayloadAction<SetTransactionsPayload>) {
            const { walletId, date, transactions } = action.payload;

            // if (!state[walletId]) {
            //     state[walletId] = {};
            // }

            if (transactions.length === 0) {
                state[walletId][date] = [];
            }
            state[walletId][date] = transactions;
        },
        addTransaction(state, action: PayloadAction<SetTransactionPayload>) {
            const { walletId, date, transaction } = action.payload;

            // if (!state[walletId]) {
            //     state[walletId] = {};
            // }
            // if (!state[walletId][date]) {
            //     state[walletId][date] = [];
            // }
            state[walletId][date].push(transaction);
        },
    },
});

export default transactionsSlice.reducer;
