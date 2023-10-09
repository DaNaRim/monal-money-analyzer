import { describe } from "@jest/globals";
import { act, fireEvent, screen, waitFor, waitForElementToBeRemoved } from "@testing-library/react";
import dayjs from "dayjs";
import { rest } from "msw";
import { setupServer } from "msw/node";
import { type ErrorResponse, ResponseErrorType } from "../../../../app/hooks/formUtils";
import { setupStore } from "../../../../app/store";
import ChangeWalletBalanceModal
    from "../../../../common/modal/ChangeWalletBalanceModal/ChangeWalletBalanceModal";
import { renderWithProviders } from "../../../../common/utils/test-utils";
import { type Category, CategoryType } from "../../../../features/category/categorySlice";
import { type Wallet } from "../../../../features/wallet/walletSlice";

const categories: Category[] = [
    {
        id: 3,
        name: "Other",
        type: CategoryType.INCOME,
        subCategories: null,
    },
    {
        id: 4,
        name: "Other",
        type: CategoryType.OUTCOME,
        subCategories: null,
    },
];

describe("ChangeWalletBalanceModal", () => {
    const handlers = [
        rest.post("/api/v1/transaction", async (req, res, ctx) => {
            const transaction = await req.json();

            if (transaction.amount === 666) {
                const error: ErrorResponse = {
                    message: "Smth went wrong",
                    type: ResponseErrorType.GLOBAL_ERROR,
                    errorCode: "code",
                    fieldName: ResponseErrorType.GLOBAL_ERROR,
                };
                return await res(ctx.status(400), ctx.json([error]));
            }
            return await res(ctx.status(200), ctx.json({
                id: 1,
                ...transaction,
            }));
        }),
    ];

    const server = setupServer(...handlers);

    beforeAll(() => server.listen());
    afterEach(() => server.resetHandlers());
    afterAll(() => server.close());

    it("render", async () => {
        const wallet: Wallet = {
            id: 1,
            balance: 100,
            name: "Wallet",
            currency: "USD",
        };
        renderWithProviders(<ChangeWalletBalanceModal open={true}
                                                      setOpen={jest.fn()}
                                                      wallet={wallet}/>);

        expect(screen.getByText("Change wallet balance")).toBeInTheDocument();
        expect(screen.getByText("Balance")).toBeInTheDocument();
        expect(screen.getByText("Change")).toBeInTheDocument();
    });

    it("change balance", async () => {
        const store = setupStore({
            wallets: {
                wallets: [
                    {
                        id: 1,
                        balance: 100,
                        name: "Wallet",
                        currency: "USD",
                    },
                ],
                isInitialized: true,
            },
            categories: {
                categories,
                isInitialized: true,
            },
        });
        const wallet: Wallet = {
            id: 1,
            balance: 100,
            name: "Wallet",
            currency: "USD",
        };
        const setOpen = jest.fn();
        renderWithProviders(<ChangeWalletBalanceModal open={true}
                                                      setOpen={setOpen}
                                                      wallet={wallet}/>, { store });

        await act(() =>
            fireEvent.change(screen.getByTestId("input-balance"), { target: { value: "200" } }));

        await act(() => fireEvent.click(screen.getByText("Change")));

        await waitForElementToBeRemoved(screen.getByText("Changing..."));

        expect(setOpen).toHaveBeenCalledTimes(1);

        expect(store.getState().wallets.wallets[0].balance).toEqual(200);

        const transactions = store.getState().transactions[1][`${dayjs().format("YYYY-MM-DD")}`];

        expect(transactions).toHaveLength(1);
        expect(transactions[0].amount).toEqual(100);
        expect(transactions[0].walletId).toEqual(1);
        expect(transactions[0].category?.id).toEqual(3);
        expect(transactions[0].description).toEqual("Balance change");
        expect(transactions[0].date).toMatch(new RegExp(`^${dayjs().format("YYYY-MM-DD")}?`));
    });

    it("change balance. Bad request", async () => {
        const store = setupStore({
            wallets: {
                wallets: [
                    {
                        id: 1,
                        balance: 0,
                        name: "Wallet",
                        currency: "USD",
                    },
                ],
                isInitialized: true,
            },
            categories: {
                categories,
                isInitialized: true,
            },
        });
        const wallet: Wallet = {
            id: 1,
            balance: 0,
            name: "Wallet",
            currency: "USD",
        };
        const setOpen = jest.fn();
        renderWithProviders(<ChangeWalletBalanceModal open={true}
                                                      setOpen={setOpen}
                                                      wallet={wallet}/>, { store });

        await act(() =>
            fireEvent.change(screen.getByTestId("input-balance"), { target: { value: "666" } }));

        await act(() => fireEvent.click(screen.getByText("Change")));

        expect(screen.getByText("Changing...")).toBeInTheDocument();
        await act(async () => await new Promise(resolve => setTimeout(resolve, 200)));

        await waitFor(() => {
            expect(screen.getByText("Change")).toBeInTheDocument();
            expect(setOpen).toHaveBeenCalledTimes(0);
        });
    });
});
