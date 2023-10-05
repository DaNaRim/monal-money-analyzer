import { createSlice, type PayloadAction } from "@reduxjs/toolkit";
import dayjs from "dayjs";
import utc from "dayjs/plugin/utc";
import { type RootState } from "../../app/store";
import { clearAuthState } from "../auth/authSlice";
import { type Category } from "../category/categorySlice";

dayjs.extend(utc);

const TRANSACTION_DATE_FORMAT = "YYYY-MM-DD HH:mm";
const GROUPED_DATE_FORMAT = "YYYY-MM-DD";

export interface Transaction {
    id: number;
    description?: string;
    date: string;
    amount: number;
    category: Category | null; // null if category is not found
    walletId: number;
}

// format in which date is received from server
export interface ViewTransactionDto {
    id: number;
    description: string;
    date: string;
    amount: number;
    categoryId: number;
    walletId: number;
}

export interface CreateTransactionDto {
    description: string;
    // Server also accepts Date, but it's easier to use string. Format: "YYYY-MM-DD HH:mm"
    date: string;
    amount: number;
    categoryId: number;
    walletId: number;
}

export interface UpdateTransactionDto extends CreateTransactionDto {
    id: number;
}

export type TransactionsState = {
    [walletId in number]: {
        [date in string]: Transaction[] // date format: "YYYY-MM-DD"
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
            transactions.forEach(transaction => {
                transaction.date
                    = dayjs.utc(transaction.date).local().format(TRANSACTION_DATE_FORMAT);
            });
            transactions.sort(sortByDate);
            state[walletId][date] = transactions;
        },
        addTransaction(state, action: PayloadAction<SetTransactionPayload>) {
            const { walletId, transaction } = action.payload;

            const date = dayjs.utc(transaction.date).local().format(GROUPED_DATE_FORMAT);

            if (state[walletId] == null) {
                state[walletId] = {};
            }
            if (state[walletId][date] == null) {
                state[walletId][date] = [];
            }
            state[walletId][date].push({
                ...transaction,
                date: dayjs.utc(transaction.date).local().format(TRANSACTION_DATE_FORMAT),
            });
            state[walletId][date].sort(sortByDate);
        },
        updateTransaction(state, action: PayloadAction<Transaction>) {
            const transaction = action.payload;
            const newDate = dayjs.utc(transaction.date).local().format(GROUPED_DATE_FORMAT);

            for (const walletId in state) {
                if (!Object.hasOwn(state, walletId)) {
                    continue;
                }
                for (const date in state[walletId]) {
                    if (!Object.hasOwn(state[walletId], date)) {
                        continue;
                    }
                    const transactions = state[walletId][date];
                    const index = transactions.findIndex(transaction0 =>
                        transaction0.id === transaction.id,
                    );
                    if (index !== -1) { // transaction found
                        if (Number(walletId) === transaction.walletId) { // wallet didn't change
                            updateTransactionWalletNotChanges(
                                state, transaction, newDate, Number(walletId), date,
                            );
                            return;
                        }
                        // wallet changed
                        updateTransactionWalletChanges(
                            state, transaction, transactions, newDate, index,
                        );
                    }
                }
            }
        },
        deleteTransaction(state, action: PayloadAction<number>) {
            const transactionId = action.payload;

            for (const walletId in state) {
                if (!Object.hasOwn(state, walletId)) {
                    continue;
                }
                for (const date in state[walletId]) {
                    if (!Object.hasOwn(state[walletId], date)) {
                        continue;
                    }
                    const transactions = state[walletId][date];
                    const index
                        = transactions.findIndex(transaction => transaction.id === transactionId);
                    if (index !== -1) {
                        transactions.splice(index, 1);
                        return;
                    }
                }
            }
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
    updateTransaction,
    deleteTransaction,
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

function updateTransactionWalletChanges(state: TransactionsState,
                                        transaction: Transaction,
                                        transactions: Transaction[],
                                        newDate: string,
                                        index: number,
) {
    transactions.splice(index, 1);

    if (state[transaction.walletId] == null) {
        state[transaction.walletId] = {};
    }
    if (state[transaction.walletId][newDate] == null) {
        state[transaction.walletId][newDate] = [];
    }
    state[transaction.walletId][newDate].push({
        ...transaction,
        date: dayjs.utc(transaction.date).local()
            .format(TRANSACTION_DATE_FORMAT),
    });
    state[transaction.walletId][newDate].sort(sortByDate);
}

function updateTransactionWalletNotChanges(state: TransactionsState,
                                           transaction: Transaction,
                                           newDate: string,
                                           walletId: number,
                                           date: string,
) {
    const transactions = state[walletId][date];
    const index = transactions.findIndex(transaction0 =>
        transaction0.id === transaction.id,
    );

    if (date === newDate) { // date didn't change
        transactions.splice(index, 1);
        transactions.push({
            ...transaction,
            date: dayjs.utc(transaction.date).local()
                .format(TRANSACTION_DATE_FORMAT),
        });
        transactions.sort(sortByDate);
        return;
    }
    // transaction date changed
    transactions.splice(index, 1);
    if (state[walletId][newDate] == null) {
        state[walletId][newDate] = [];
    }
    state[walletId][newDate].push({
        ...transaction,
        date: dayjs.utc(transaction.date).local()
            .format(TRANSACTION_DATE_FORMAT),
    });
    state[walletId][newDate].sort(sortByDate);
}
