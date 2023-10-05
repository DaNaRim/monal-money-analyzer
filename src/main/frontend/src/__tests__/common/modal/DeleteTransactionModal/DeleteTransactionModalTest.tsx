import { describe } from "@jest/globals";
import { act, screen, waitForElementToBeRemoved } from "@testing-library/react";
import { rest } from "msw";
import { setupServer } from "msw/node";
import { type ErrorResponse, ResponseErrorType } from "../../../../app/hooks/formUtils";
import { setupStore } from "../../../../app/store";
import DeleteTransactionModal
    from "../../../../common/modal/DeleteTransactionModal/DeleteTransactionModal";
import { renderWithProviders } from "../../../../common/utils/test-utils";
import { CategoryType } from "../../../../features/category/categorySlice";

describe("DeleteTransactionModal", () => {
    const handlers = [
        rest.delete("/api/v1/transaction", async (req, res, ctx) => {
            const transactionId = Number(req.url.searchParams.get("transactionId"));

            if (transactionId === -1) {
                res.networkError("Failed to connect");
                return;
            }
            if (transactionId === -2) {
                const error: ErrorResponse = {
                    message: "Error message",
                    type: ResponseErrorType.GLOBAL_ERROR,
                    errorCode: "code",
                    fieldName: ResponseErrorType.GLOBAL_ERROR,
                };
                return await res(ctx.status(400), ctx.json([error]));
            }
            return await res(ctx.status(200));
        }),
    ];

    const server = setupServer(...handlers);

    jest.useFakeTimers();

    beforeAll(() => server.listen());
    afterEach(() => server.resetHandlers());
    afterAll(() => server.close());

    test("render", () => {
        renderWithProviders(<DeleteTransactionModal open={true}
                                                    setOpen={jest.fn()}
                                                    transactionId={1}/>);

        expect(screen.getByText("Delete transaction?")).toBeInTheDocument();
        expect(screen.getByText("Cancel")).toBeInTheDocument();
        expect(screen.getByText("Delete")).toBeInTheDocument();
    });

    test("cancel deleting", async () => {
        const setOpen = jest.fn();

        renderWithProviders(<DeleteTransactionModal open={true}
                                                    setOpen={setOpen}
                                                    transactionId={1}/>);

        act(() => screen.getByText("Cancel").click());

        expect(setOpen).toBeCalledWith(false);
    });

    test("delete transaction", async () => {
        const setOpen = jest.fn();

        const store = setupStore({
            transactions: {
                1: {
                    "2023-01-01": [
                        {
                            id: 1,
                            date: "2023-01-01",
                            amount: 100,
                            description: "Description",
                            category: {
                                id: 1,
                                name: "Category",
                                type: CategoryType.OUTCOME,
                                subCategories: null,
                            },
                        },
                    ],
                },
            },
        });
        renderWithProviders(<DeleteTransactionModal open={true}
                                                    setOpen={setOpen}
                                                    transactionId={1}/>, { store });

        act(() => screen.getByText("Delete").click());

        await waitForElementToBeRemoved(screen.getByText("Deleting..."));

        expect(store.getState().transactions).toEqual({
            1: {
                "2023-01-01": [],
            },
        });
        expect(setOpen).toBeCalledWith(false);
    });

    test("delete transaction. BadRequest -> display error", async () => {
        const setOpen = jest.fn();

        const transactions = {
            1: {
                "2023-01-01": [
                    {
                        id: 1,
                        date: "2023-01-01",
                        amount: 100,
                        description: "Description",
                        category: {
                            id: 1,
                            name: "Category",
                            type: CategoryType.OUTCOME,
                            subCategories: null,
                        },
                    },
                ],
            },
        };
        const store = setupStore({ transactions });

        renderWithProviders(<DeleteTransactionModal open={true}
                                                    setOpen={setOpen}
                                                    transactionId={-2}/>, { store });

        act(() => screen.getByText("Delete").click());

        await waitForElementToBeRemoved(screen.getByText("Deleting..."));

        expect(screen.getByText("Error message")).toBeInTheDocument();
        expect(setOpen).toBeCalledTimes(0);

        await act(() => jest.advanceTimersByTime(2_000));
        expect(setOpen).toBeCalledWith(false);

        await act(() => jest.advanceTimersByTime(500));
        expect(screen.queryByText("Error message")).not.toBeInTheDocument();
        expect(store.getState().transactions).toEqual(transactions);
    });

    test("delete transaction. networkError -> display error", async () => {
        const setOpen = jest.fn();

        const transactions = {
            1: {
                "2023-01-01": [
                    {
                        id: 1,
                        date: "2023-01-01",
                        amount: 100,
                        description: "Description",
                        category: {
                            id: 1,
                            name: "Category",
                            type: CategoryType.OUTCOME,
                            subCategories: null,
                        },
                    },
                ],
            },
        };
        const store = setupStore({ transactions });

        renderWithProviders(<DeleteTransactionModal open={true}
                                                    setOpen={setOpen}
                                                    transactionId={-1}/>, { store });

        act(() => screen.getByText("Delete").click());

        await waitForElementToBeRemoved(screen.getByText("Deleting..."));

        expect(screen.getByText("Failed to connect to server. Please try again later."))
            .toBeInTheDocument();
        expect(setOpen).toBeCalledTimes(0);

        await act(() => jest.advanceTimersByTime(2_000));
        expect(setOpen).toBeCalledWith(false);

        await act(() => jest.advanceTimersByTime(500));
        expect(screen.queryByText("Failed to connect to server. Please try again later."))
            .not.toBeInTheDocument();
        expect(store.getState().transactions).toEqual(transactions);
    });
});