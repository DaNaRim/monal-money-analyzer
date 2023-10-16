import { describe } from "@jest/globals";
import { act, fireEvent, screen, waitForElementToBeRemoved } from "@testing-library/react";
import dayjs from "dayjs";
import { rest } from "msw";
import { setupServer } from "msw/node";
import { type ErrorResponse, ResponseErrorType } from "../../../../app/hooks/formUtils";
import { setupStore } from "../../../../app/store";
import UpdateTransactionModal
    from "../../../../common/modal/UpdateTransactionModal/UpdateTransactionModal";
import { renderWithProviders } from "../../../../common/utils/test-utils";
import { type CategoryState, CategoryType } from "../../../../features/category/categorySlice";
import { type WalletsState } from "../../../../features/wallet/walletSlice";
import {
    fillCreateUpdateTransactionForm,
} from "../CreateTransactionModal/CreateTransactionModalTest";

const walletsState: WalletsState = {
    wallets: [
        {
            id: 1,
            name: "Wallet 1",
            balance: 1000,
            currency: "USD",
        },
        {
            id: 2,
            name: "Wallet 2",
            balance: 1000,
            currency: "UAH",
        },
    ],
    isInitialized: true,
};

const walletsStateWithSameCurrency: WalletsState = {
    wallets: [
        {
            id: 1,
            name: "Wallet 1",
            balance: 1000,
            currency: "USD",
        },
        {
            id: 2,
            name: "Wallet 2",
            balance: 1000,
            currency: "USD",
        },
        {
            id: 3,
            name: "Wallet 3",
            balance: 1000,
            currency: "UAH",
        },
    ],
    isInitialized: true,
};

const categoriesState: CategoryState = {
    categories: [
        {
            id: 1,
            name: "Food and beverages",
            type: CategoryType.OUTCOME,
            subCategories: [],
        },
        {
            id: 2,
            name: "Salary",
            type: CategoryType.INCOME,
            subCategories: [],
        },
    ],
    isInitialized: true,
};

const transactionOutcomeState = {
    1: {
        "2021-08-01": [
            {
                id: 1,
                walletId: 1,
                category: categoriesState.categories[0],
                date: dayjs("2021-08-01 00:00")
                    .add(dayjs().hour(), "hour")
                    .add(dayjs().minute(), "minute")
                    .format("YYYY-MM-DD HH:mm"),
                amount: 100,
                description: "Desc",
            },
        ],
    },
};

const transactionIncomeState = {
    1: {
        "2021-08-01": [
            {
                id: 1,
                walletId: 1,
                category: categoriesState.categories[1],
                date: "2021-08-01 00:00",
                amount: 100,
                description: "Desc",
            },
        ],
    },
};

describe("UpdateTransactionModal", () => {
    const handlers = [
        rest.put("/api/v1/transaction", async (req, res, ctx) => {
            const transaction = await req.json();

            if (transaction.description === "globalError") {
                const error: ErrorResponse = {
                    message: "Smth went wrong",
                    type: ResponseErrorType.GLOBAL_ERROR,
                    errorCode: "code",
                    fieldName: ResponseErrorType.GLOBAL_ERROR,
                };
                return await res(ctx.status(400), ctx.json([error]));
            }
            if (transaction.description === "serverError") {
                const error: ErrorResponse = {
                    message: "Smth went wrong",
                    type: ResponseErrorType.SERVER_ERROR,
                    errorCode: "code",
                    fieldName: ResponseErrorType.SERVER_ERROR,
                };
                return await res(ctx.status(500), ctx.json([error]));
            }
            const result = {
                ...transaction,
                id: 1,
                amount: Number(transaction.amount),
            };
            return await res(ctx.status(200), ctx.json(result), ctx.delay(10));
        }),
    ];

    const server = setupServer(...handlers);

    beforeAll(() => server.listen());
    afterEach(() => server.resetHandlers());
    afterAll(() => server.close());

    jest.useFakeTimers();

    it("render", () => {
        const store = setupStore({
            wallets: walletsState,
            categories: categoriesState,
            transactions: transactionOutcomeState,
        });
        renderWithProviders(
            <UpdateTransactionModal open={true}
                                    setOpen={jest.fn()}
                                    transaction={transactionOutcomeState["1"]["2021-08-01"][0]}/>,
            { store },
        );
        expect(screen.getByText("Edit transaction")).toBeInTheDocument();

        // Form
        expect(screen.getAllByText("Wallet")).toHaveLength(2); // label and legend
        expect(screen.getByText("Category")).toBeInTheDocument();
        expect(screen.getByText("Amount")).toBeInTheDocument();
        expect(screen.getByText("Date")).toBeInTheDocument();
        expect(screen.getByText("Description")).toBeInTheDocument();
        expect(screen.getByText("Update")).toBeInTheDocument();
        // If only one wallet with currency like in transaction - disable wallet select
        expect(document.querySelector(".input_select > input")).toHaveAttribute("disabled");
    });

    it("render. Few wallets with same currency like in transactions", () => {
        const store = setupStore({
            wallets: walletsStateWithSameCurrency,
            categories: categoriesState,
            transactions: transactionOutcomeState,
        });
        renderWithProviders(
            <UpdateTransactionModal open={true}
                                    setOpen={jest.fn()}
                                    transaction={transactionOutcomeState["1"]["2021-08-01"][0]}/>,
            { store },
        );
        expect(document.querySelector(".input_select > input")).not.toHaveAttribute("disabled");
        act(() => {
            fireEvent.mouseDown(document.querySelector(".MuiSelect-select") as Element);
        });
        expect(screen.getAllByText("Wallet 1")[1]).toBeInTheDocument(); // first is in display
        expect(screen.getByText("Wallet 2")).toBeInTheDocument();
        expect(screen.queryByText("Wallet 3")).not.toBeInTheDocument(); // Different currency
    });

    it("update transaction", async () => {
        const store = setupStore({
            wallets: walletsState,
            categories: categoriesState,
            transactions: transactionOutcomeState,
        });
        const setOpen = jest.fn();
        renderWithProviders(
            <UpdateTransactionModal open={true}
                                    setOpen={setOpen}
                                    transaction={transactionOutcomeState["1"]["2021-08-01"][0]}/>,
            { store },
        );
        fillCreateUpdateTransactionForm("Food and beverages", 100, "Test");

        await act(() => fireEvent.click(screen.getByText("Update")));

        await waitForElementToBeRemoved(() => screen.getByText("Updating..."), { timeout: 2000 });

        expect(store.getState().transactions).toEqual({
            1: {
                "2021-08-01": [
                    {
                        id: 1,
                        walletId: 1,
                        category: categoriesState.categories[0],
                        date: expect.any(String),
                        amount: 100,
                        description: "Test",
                        categoryId: 1,
                    },
                ],
            },
        });
        expect(setOpen).toBeCalledWith(false); // Close modal
    });

    it("update transaction. Global error", async () => {
        const store = setupStore({
            wallets: walletsState,
            categories: categoriesState,
            transactions: transactionOutcomeState,
        });
        renderWithProviders(
            <UpdateTransactionModal open={true}
                                    setOpen={jest.fn()}
                                    transaction={transactionOutcomeState["1"]["2021-08-01"][0]}/>,
            { store },
        );
        fillCreateUpdateTransactionForm("Food and beverages", 100, "globalError");

        await act(() => fireEvent.click(screen.getByText("Update")));

        await waitForElementToBeRemoved(() => screen.getByText("Updating..."), { timeout: 2000 });

        expect(store.getState().transactions).toStrictEqual(transactionOutcomeState);
        expect(screen.getByText("Smth went wrong")).toBeInTheDocument();
    });

    it("update transaction. Server error", async () => {
        const store = setupStore({
            wallets: walletsState,
            categories: categoriesState,
            transactions: transactionOutcomeState,
        });
        renderWithProviders(
            <UpdateTransactionModal open={true}
                                    setOpen={jest.fn()}
                                    transaction={transactionOutcomeState["1"]["2021-08-01"][0]}/>,
            { store },
        );
        fillCreateUpdateTransactionForm("Food and beverages", 100, "serverError");

        await act(() => fireEvent.click(screen.getByText("Update")));

        await waitForElementToBeRemoved(() => screen.getByText("Updating..."), { timeout: 2000 });

        expect(store.getState().transactions).toStrictEqual(transactionOutcomeState);
        expect(screen.getByText("Server error. Please try again later."
            + " If the problem persists, please contact the administrator")).toBeInTheDocument();
    });

    it("update transaction. Date changed", async () => {
        const store = setupStore({
            wallets: walletsState,
            categories: categoriesState,
            transactions: transactionOutcomeState,
        });
        renderWithProviders(
            <UpdateTransactionModal open={true}
                                    setOpen={jest.fn()}
                                    transaction={transactionOutcomeState["1"]["2021-08-01"][0]}/>,
            { store },
        );
        act(() => {
            fireEvent.change(screen.getByTestId("input-date"),
                { target: { value: "2021-08-02 00:00" } });
        });
        await act(() => fireEvent.click(screen.getByText("Update")));

        await waitForElementToBeRemoved(() => screen.getByText("Updating..."),
            { timeout: 5000 });

        expect(store.getState().transactions).toStrictEqual({
            1: {
                "2021-08-01": [],
                "2021-08-02": [
                    {
                        id: 1,
                        walletId: 1,
                        category: categoriesState.categories[0],
                        date: "2021-08-02 00:00",
                        amount: 100,
                        description: "Desc",
                        categoryId: categoriesState.categories[0].id,
                    },
                ],
            },
        });
    }, 10_000);

    it("update transaction. Outcome amount changed Up", async () => {
        const store = setupStore({
            wallets: walletsState,
            categories: categoriesState,
            transactions: transactionOutcomeState,
        });
        renderWithProviders(
            <UpdateTransactionModal open={true}
                                    setOpen={jest.fn()}
                                    transaction={transactionOutcomeState["1"]["2021-08-01"][0]}/>,
            { store },
        );
        act(() => {
            fireEvent.change(screen.getByTestId("input-amount"), { target: { value: 50 } });
        });
        await act(() => fireEvent.click(screen.getByText("Update")));

        await waitForElementToBeRemoved(() => screen.getByText("Updating..."), { timeout: 5000 });

        expect(store.getState().transactions)
            .toStrictEqual(changedState(50, 0));
        // old: Outcome 100, new: Outcome 50,
        // old balance: 1000, new balance: 1000 + (-50 - -100) = 1050
        expect(store.getState().wallets.wallets[0].balance).toBe(1050);
    }, 10_000);

    it("update transaction. Outcome amount changed Down", async () => {
        const store = setupStore({
            wallets: walletsState,
            categories: categoriesState,
            transactions: transactionOutcomeState,
        });
        renderWithProviders(
            <UpdateTransactionModal open={true}
                                    setOpen={jest.fn()}
                                    transaction={transactionOutcomeState["1"]["2021-08-01"][0]}/>,
            { store },
        );
        act(() => {
            fireEvent.change(screen.getByTestId("input-amount"), { target: { value: 200 } });
        });
        await act(() => fireEvent.click(screen.getByText("Update")));

        await waitForElementToBeRemoved(() => screen.getByText("Updating..."),
            { timeout: 5000 });

        expect(store.getState().transactions)
            .toStrictEqual(changedState(200, 0));
        // old: Outcome 100, new: Outcome 50,
        // old balance: 1000, new balance: 1000 + (-200 - -100) = 900
        expect(store.getState().wallets.wallets[0].balance).toBe(900);
    }, 10_000);

    it("update transaction. Income amount changed Up", async () => {
        const store = setupStore({
            wallets: walletsState,
            categories: categoriesState,
            transactions: transactionIncomeState,
        });
        renderWithProviders(
            <UpdateTransactionModal open={true}
                                    setOpen={jest.fn()}
                                    transaction={transactionIncomeState["1"]["2021-08-01"][0]}/>,
            { store },
        );
        act(() => {
            fireEvent.change(screen.getByTestId("input-amount"), { target: { value: 200 } });
        });
        await act(() => fireEvent.click(screen.getByText("Update")));

        await waitForElementToBeRemoved(() => screen.getByText("Updating..."),
            { timeout: 5000 });

        expect(store.getState().transactions)
            .toStrictEqual(changedState(200, 1));
        // old: Income 100, new: Income 200,
        // old balance: 1000, new balance: 1000 + (200 - 100) = 1100
        expect(store.getState().wallets.wallets[0].balance).toBe(1100);
    }, 10_000);

    it("update transaction. Income amount changed Down", async () => {
        const store = setupStore({
            wallets: walletsState,
            categories: categoriesState,
            transactions: transactionIncomeState,
        });
        renderWithProviders(
            <UpdateTransactionModal open={true}
                                    setOpen={jest.fn()}
                                    transaction={transactionIncomeState["1"]["2021-08-01"][0]}/>,
            { store },
        );
        act(() => {
            fireEvent.change(screen.getByTestId("input-amount"), { target: { value: 50 } });
        });
        await act(() => fireEvent.click(screen.getByText("Update")));

        await waitForElementToBeRemoved(() => screen.getByText("Updating..."),
            { timeout: 5000 });

        expect(store.getState().transactions)
            .toStrictEqual(changedState(50, 1));
        // old: Income 100, new: Income 50,
        // old balance: 1000, new balance: 1000 + (50 - 100) = 950
        expect(store.getState().wallets.wallets[0].balance).toBe(950);
    }, 10_000);

    it("update transaction. category changes income to outcome", async () => {
        const store = setupStore({
            wallets: walletsState,
            categories: categoriesState,
            transactions: transactionIncomeState,
        });
        renderWithProviders(
            <UpdateTransactionModal open={true}
                                    setOpen={jest.fn()}
                                    transaction={transactionIncomeState["1"]["2021-08-01"][0]}/>,
            { store },
        );
        fillCreateUpdateTransactionForm("Food and beverages", 200, "Desc");
        await act(() => fireEvent.click(screen.getByText("Update")));

        await waitForElementToBeRemoved(() => screen.getByText("Updating..."),
            { timeout: 5000 });

        expect(store.getState().transactions)
            .toStrictEqual(changedState(200, 0));
        // old: Income 100, new: Outcome 200,
        // old balance: 1000, new balance: 1000 + (-200 - 100) = 700
        expect(store.getState().wallets.wallets[0].balance).toBe(700);
    }, 10_000);

    it("update transaction. category changes outcome to income", async () => {
        const store = setupStore({
            wallets: walletsState,
            categories: categoriesState,
            transactions: transactionOutcomeState,
        });
        renderWithProviders(
            <UpdateTransactionModal open={true}
                                    setOpen={jest.fn()}
                                    transaction={transactionOutcomeState["1"]["2021-08-01"][0]}/>,
            { store },
        );
        fillCreateUpdateTransactionForm("Salary", 200, "Desc", "Income");
        await act(() => fireEvent.click(screen.getByText("Update")));

        await waitForElementToBeRemoved(() => screen.getByText("Updating..."), { timeout: 5000 });

        expect(store.getState().transactions)
            .toStrictEqual(changedState(200, 1));
        // old: Outcome 100, new: Income 200,
        // old balance: 1000, new balance: 1000 + (200 - -100) = 1300
        expect(store.getState().wallets.wallets[0].balance).toBe(1300);
    }, 10_000);

    it("update transaction. wallet changes outcome", async () => {
        const store = setupStore({
            wallets: walletsStateWithSameCurrency,
            categories: categoriesState,
            transactions: transactionOutcomeState,
        });
        renderWithProviders(
            <UpdateTransactionModal open={true}
                                    setOpen={jest.fn()}
                                    transaction={transactionOutcomeState["1"]["2021-08-01"][0]}/>,
            { store },
        );
        await selectWallet("Wallet 2");
        await act(() => fireEvent.click(screen.getByText("Update")));

        await waitForElementToBeRemoved(() => screen.getByText("Updating..."),
            { timeout: 5000 });

        expect(store.getState().transactions).toStrictEqual({
            1: {
                "2021-08-01": [],
            },
            2: {
                "2021-08-01": [
                    changedTransaction(2, 100, 0),
                ],
            },
        });
        // old: Outcome 100, new: 0,
        // old balance: 1000, new balance: 1000 + (0 - -100) = 1100
        expect(store.getState().wallets.wallets[0].balance).toBe(1100);
        // old: 0, new: Outcome 100,
        // old balance: 1000, new balance: 1000 + (-100 - 0) = 900
        expect(store.getState().wallets.wallets[1].balance).toBe(900);
    }, 10_000);

    it("update transaction. wallet changes income", async () => {
        const store = setupStore({
            wallets: walletsStateWithSameCurrency,
            categories: categoriesState,
            transactions: transactionIncomeState,
        });
        renderWithProviders(
            <UpdateTransactionModal open={true}
                                    setOpen={jest.fn()}
                                    transaction={transactionIncomeState["1"]["2021-08-01"][0]}/>,
            { store },
        );
        await selectWallet("Wallet 2");
        await act(() => fireEvent.click(screen.getByText("Update")));

        await waitForElementToBeRemoved(() => screen.getByText("Updating..."), { timeout: 5000 });

        expect(store.getState().transactions).toStrictEqual({
            1: {
                "2021-08-01": [],
            },
            2: {
                "2021-08-01": [
                    changedTransaction(2, 100, 1),
                ],
            },
        });
        // old: Income 100, new: 0,
        // old balance: 1000, new balance: 1000 + (0 - 100) = 900
        expect(store.getState().wallets.wallets[0].balance).toBe(900);
        // old: 0, new: Income 100,
        // old balance: 1000, new balance: 1000 + (100 - 0) = 1100
        expect(store.getState().wallets.wallets[1].balance).toBe(1100);
    }, 10_000);
});

async function selectWallet(walletName: string) {
    await act(() => fireEvent.mouseDown(document.querySelector(".MuiSelect-select") as Element));
    await act(() => fireEvent.click(screen.getByText(walletName)));
}

function changedState(amount: number, categoryId: number) {
    return {
        1: {
            "2021-08-01": [
                changedTransaction(undefined, amount, categoryId),
            ],
        },
    };
}

function changedTransaction(walletId: number = 1, amount: number, categoryId: number) {
    return {
        id: 1,
        walletId,
        category: categoriesState.categories[categoryId],
        date: expect.any(String),
        amount,
        description: "Desc",
        categoryId: categoriesState.categories[categoryId].id,
    };
}
