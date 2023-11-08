import { describe } from "@jest/globals";
import { act, fireEvent, screen, waitFor, waitForElementToBeRemoved } from "@testing-library/react";
import dayjs from "dayjs";
import { rest } from "msw";
import { setupServer } from "msw/node";
import { setupStore } from "../../../../app/store";
import CreateTransactionModal
    from "../../../../common/modal/CreateTransactionModal/CreateTransactionModal";
import { type ErrorResponse, ResponseErrorType } from "../../../../common/utils/formUtils";
import { renderWithProviders } from "../../../../common/utils/test-utils";
import { type CategoryState, CategoryType } from "../../../../features/category/categorySlice";
import { type WalletsState } from "../../../../features/wallet/walletSlice";

const walletsState: WalletsState = {
    wallets: [
        {
            id: 1,
            name: "Wallet 1",
            balance: 1000,
            currency: "USD",
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
    ],
    isInitialized: true,
};

describe("CreateTransactionModal", () => {
    const handlers = [
        rest.post("/api/v1/transaction", async (req, res, ctx) => {
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
            return await res(ctx.status(200), ctx.json(result), ctx.delay(50));
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
        });
        renderWithProviders(<CreateTransactionModal open={true}
                                                    date={dayjs().format("YYYY-MM-DD")}
                                                    setOpen={jest.fn()}
                                                    walletId={1}/>, { store });

        expect(screen.getByText("Create transaction")).toBeInTheDocument();

        // Form
        expect(screen.getAllByText("Wallet")).toHaveLength(2); // label and legend
        expect(screen.getByText("Category")).toBeInTheDocument();
        expect(screen.getByText("Amount")).toBeInTheDocument();
        expect(screen.getByText("Date")).toBeInTheDocument();
        expect(screen.getByText("Description")).toBeInTheDocument();
        expect(screen.getByText("Create")).toBeInTheDocument();
    });

    it("create transaction", async () => {
        const setOpen = jest.fn();

        const store = setupStore({
            wallets: walletsState,
            categories: categoriesState,
        });
        renderWithProviders(<CreateTransactionModal open={true}
                                                    date={dayjs().format("YYYY-MM-DD")}
                                                    setOpen={setOpen}
                                                    walletId={1}/>, { store });

        // Fill form (Wallet and date are filled by default)
        fillCreateUpdateTransactionForm("Food and beverages", 100, "Test");
        await act(() => fireEvent.click(screen.getByText("Create")));

        await waitForElementToBeRemoved(() => screen.getByText("Creating..."),
            { timeout: 10000 });

        expect(screen.getByText("Transaction created successfully.")).toBeInTheDocument();
        expect(screen.queryByText("Create transaction")).not.toBeInTheDocument();

        await act(() => jest.advanceTimersByTime(1500));

        await waitFor(() => {
            const transactionByDate
                = store.getState().transactions?.["1"]?.[dayjs().format("YYYY-MM-DD")];

            expect(transactionByDate).toHaveLength(1);
            expect(transactionByDate?.[0]).toEqual({
                id: 1,
                category: expect.any(Object),
                amount: 100,
                description: "Test",
                date: dayjs().subtract(4, "seconds").format("YYYY-MM-DD HH:mm"),
                categoryId: expect.any(Number), // Useless
                walletId: 1,
            });
            expect(setOpen).toBeCalledWith(false);
        });
    }, 20_000);

    it("create transaction. Global Error", async () => {
        const setOpen = jest.fn();

        const store = setupStore({
            wallets: walletsState,
            categories: categoriesState,
        });
        renderWithProviders(<CreateTransactionModal open={true}
                                                    date={dayjs().format("YYYY-MM-DD")}
                                                    setOpen={setOpen}
                                                    walletId={1}/>, { store });

        // Fill form (Wallet and date are filled by default)
        fillCreateUpdateTransactionForm("Food and beverages", 100, "globalError");
        await act(() => fireEvent.click(screen.getByText("Create")));

        expect(await screen.findByText("Creating...")).toBeInTheDocument();

        await waitFor(() => {
            expect(screen.queryByText("Transaction created successfully.")).not.toBeInTheDocument();
            expect(screen.getByText("Create transaction")).toBeInTheDocument();
            expect(screen.getByTestId("global-error-message")).toBeInTheDocument();
        });
        expect(setOpen).not.toBeCalled();
    });

    it("create transaction. Server Error", async () => {
        const setOpen = jest.fn();

        const store = setupStore({
            wallets: walletsState,
            categories: categoriesState,
        });
        renderWithProviders(<CreateTransactionModal open={true}
                                                    date={dayjs().format("YYYY-MM-DD")}
                                                    setOpen={setOpen}
                                                    walletId={1}/>, { store });

        // Fill form (Wallet and date are filled by default)
        fillCreateUpdateTransactionForm("Food and beverages", 100, "serverError");
        await act(() => fireEvent.click(screen.getByText("Create")));

        expect(await screen.findByText("Creating...")).toBeInTheDocument();

        await waitFor(() => {
            expect(screen.queryByText("Transaction created successfully.")).not.toBeInTheDocument();
            expect(screen.getByText("Create transaction")).toBeInTheDocument();
            expect(screen.getByTestId("server-error-message")).toBeInTheDocument();
        });
        expect(setOpen).not.toBeCalled();
    });
});

export function fillCreateUpdateTransactionForm(category: string,
                                                amount: number,
                                                description: string,
                                                categoryType: "Outcome" | "Income" = "Outcome",
) {
    act(() => {
        fireEvent.click(screen.getByText("Category")); // Open category modal
    });
    act(() => {
        fireEvent.click(screen.getByText(categoryType)); // Select category type
    });
    act(() => { // Select category
        fireEvent.click(
            screen.getAllByText(category).filter(element => element.tagName === "DIV")[0],
        );
    });
    act(() => {
        fireEvent.change(screen.getByTestId("input-amount"), { target: { value: amount } });
        fireEvent.change(screen.getByTestId("input-description"),
            { target: { value: description } });
    });
}
