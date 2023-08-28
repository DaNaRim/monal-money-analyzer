import { describe } from "@jest/globals";
import dayjs from "dayjs";
import { setupStore } from "../../../app/store";
import { CategoryType } from "../../../features/category/categorySlice";
import reducer, {
    addTransaction,
    saveTransactions,
    selectIsTransactionsInitByWalletAndDate,
    selectTransactionsByWalletAndDate,
    type Transaction,
    type TransactionsState,
} from "../../../features/transaction/transactionSlice";

describe("transactionSlice", () => {
    test("initialState", () => {
        expect(reducer(undefined, { type: undefined })).toEqual({});
    });

    test("saveTransactions", () => {
        const transactions: Transaction[] = [
            {
                id: 1,
                date: dayjs("2021-01-01 12:38").toDate(),
                description: "test",
                amount: 100,
                category: {
                    id: 1,
                    name: "test",
                    type: CategoryType.INCOME,
                    subCategories: null,
                },
            },
            {
                id: 2,
                date: dayjs("2021-01-01 12:40").toDate(),
                description: "test",
                amount: 100,
                category: null,
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
                        date: dayjs("2021-01-01 12:38").toDate(),
                        description: "test",
                        amount: 100,
                        category: null,
                    },
                ],
            },
        };
        const transaction: Transaction = {
            id: 2,
            date: dayjs("2021-01-01 12:40").toDate(),
            description: "test",
            amount: 100,
            category: null,
        };
        const setTransactionPayload = {
            walletId: 1,
            transaction,
        };
        expect(reducer(prevState, addTransaction(setTransactionPayload))).toEqual({
            1: {
                // transactions are sorted by date
                "2021-01-01": [transaction, ...prevState[1]["2021-01-01"]],
            },
        });
    });

    test("selectIsTransactionsInitByWalletAndDate", () => {
        const preloadedState: TransactionsState = {
            1: {
                "2021-01-01": [
                    {
                        id: 1,
                        date: dayjs("2021-01-01 12:38").toDate(),
                        description: "test",
                        amount: 100,
                        category: null,
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
