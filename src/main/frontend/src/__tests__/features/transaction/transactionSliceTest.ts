import { describe } from "@jest/globals";
import dayjs from "dayjs";
import { setupStore } from "../../../app/store";
import { CategoryType } from "../../../features/category/categorySlice";
import reducer, {
    addTransaction,
    deleteTransaction,
    saveTransactions,
    selectIsTransactionsInitByWalletAndDate,
    selectTransactionsByWalletAndDate,
    type Transaction,
    type TransactionsState,
    updateTransaction,
} from "../../../features/transaction/transactionSlice";

describe("transactionSlice", () => {
    test("initialState", () => {
        expect(reducer(undefined, { type: undefined })).toEqual({});
    });

    test("saveTransactions", () => {
        const transactions: Transaction[] = [
            {
                id: 1,
                date: "2021-01-01 12:38",
                description: "test",
                amount: 100,
                category: {
                    id: 1,
                    name: "test",
                    type: CategoryType.INCOME,
                    subCategories: null,
                },
                walletId: 1,
            },
            {
                id: 2,
                date: "2021-01-01 12:40",
                description: "test",
                amount: 100,
                category: null,
                walletId: 1,
            },
        ];
        const setTransactionsPayload = {
            walletId: 1,
            transactions,
            date: "2021-01-01",
        };
        expect(reducer(undefined, saveTransactions(setTransactionsPayload))).toEqual({
            1: {
                "2021-01-01": transactions,
            },
        });
    });

    test("addTransaction", () => {
        const prevState = {
            1: {
                "2021-01-01": [
                    {
                        id: 1,
                        date: "2021-01-01 12:38",
                        description: "test",
                        amount: 100,
                        category: null,
                        walletId: 1,
                    },
                ],
            },
        };
        const transaction: Transaction = {
            id: 2,
            date: "2021-01-01 12:40",
            description: "test",
            amount: 100,
            category: null,
            walletId: 1,
        };
        const setTransactionPayload = {
            walletId: 1,
            transaction,
        };
        expect(reducer(prevState, addTransaction(setTransactionPayload))).toEqual({
            1: {
                // transactions are sorted by date
                "2021-01-01": [
                    {
                        ...transaction,
                        date: dayjs.utc(transaction.date).local().format("YYYY-MM-DD HH:mm"),
                    },
                    ...prevState[1]["2021-01-01"],
                ],
            },
        });
    });

    test("deleteTransaction", () => {
        const transaction2: Transaction = {
            id: 2,
            date: "2021-01-02 12:38",
            description: "test",
            amount: 100,
            category: null,
            walletId: 1,
        };
        const prevState = {
            1: {
                "2021-01-01": [
                    {
                        id: 1,
                        date: "2021-01-01 12:38",
                        description: "test",
                        amount: 100,
                        category: null,
                        walletId: 1,
                    },
                    transaction2,
                ],
            },
        };
        expect(reducer(prevState, deleteTransaction(1))).toEqual({
            1: {
                "2021-01-01": [transaction2],
            },
        });
    });

    test("updateTransaction", () => {
        const prevState = {
            1: {
                "2021-01-01": [
                    {
                        id: 1,
                        date: "2021-01-01 12:38",
                        description: "test",
                        amount: 100,
                        category: null,
                        walletId: 1,
                    },
                ],
            },
        };
        const transaction: Transaction = {
            id: 1,
            date: "2021-01-01 12:38",
            description: "updated",
            amount: 200,
            category: null,
            walletId: 1,
        };
        expect(reducer(prevState, updateTransaction(transaction))).toEqual({
            1: {
                "2021-01-01": [
                    {
                        ...transaction,
                        date: dayjs.utc(transaction.date).local().format("YYYY-MM-DD HH:mm"),
                    },
                ],
            },
        });
    });

    test("updateTransaction. update date", () => {
        const prevState = {
            1: {
                "2021-01-01": [
                    {
                        id: 1,
                        date: "2021-01-01 12:38",
                        description: "test",
                        amount: 100,
                        category: null,
                        walletId: 1,
                    },
                ],
            },
        };
        const transaction: Transaction = {
            id: 1,
            date: "2021-01-02 12:38",
            description: "updated",
            amount: 200,
            category: null,
            walletId: 1,
        };
        expect(reducer(prevState, updateTransaction(transaction))).toEqual({
            1: {
                // The date is changed, so transaction is removed from previous date
                "2021-01-01": [],
                "2021-01-02": [
                    {
                        ...transaction,
                        date: dayjs.utc(transaction.date).local().format("YYYY-MM-DD HH:mm"),
                    },
                ],
            },
        });
    });

    test("updateTransaction. update wallet", () => {
        const prevState = {
            1: {
                "2021-01-01": [
                    {
                        id: 1,
                        date: "2021-01-01 12:38",
                        description: "test",
                        amount: 100,
                        category: null,
                        walletId: 1,
                    },
                ],
            },
        };
        const transaction: Transaction = {
            id: 1,
            date: "2021-01-01 12:38",
            description: "updated",
            amount: 200,
            category: null,
            walletId: 2,
        };
        expect(reducer(prevState, updateTransaction(transaction))).toEqual({
            1: {
                "2021-01-01": [],
            },
            2: {
                "2021-01-01": [
                    {
                        ...transaction,
                        date: dayjs.utc(transaction.date).local().format("YYYY-MM-DD HH:mm"),
                    },
                ],
            },
        });
    });

    test("selectIsTransactionsInitByWalletAndDate", () => {
        const preloadedState: TransactionsState = {
            1: {
                "2021-01-01": [
                    {
                        id: 1,
                        date: "2021-01-01 12:38",
                        description: "test",
                        amount: 100,
                        category: null,
                        walletId: 1,
                    },
                ],
            },
        };
        const store = setupStore({
            transactions: preloadedState,
        });
        expect(selectIsTransactionsInitByWalletAndDate(store.getState(), 1, "2021-01-01"))
            .toBe(true);
        expect(selectIsTransactionsInitByWalletAndDate(store.getState(), 1, "2021-01-02"))
            .toBe(false);
        expect(selectIsTransactionsInitByWalletAndDate(store.getState(), 2, "2021-01-01"))
            .toBe(false);
    });

    test("selectTransactionsByWalletAndDate with empty state", () => {
        const store = setupStore({
            transactions: {},
        });
        expect(selectTransactionsByWalletAndDate(store.getState(), 1, "2021-01-01"))
            .toStrictEqual([]);
    });
});
