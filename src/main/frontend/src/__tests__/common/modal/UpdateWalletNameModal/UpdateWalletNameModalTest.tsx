import { describe } from "@jest/globals";
import { screen } from "@testing-library/dom";
import { act, fireEvent, waitFor, waitForElementToBeRemoved } from "@testing-library/react";
import { rest } from "msw";
import { setupServer } from "msw/node";
import { setupStore } from "../../../../app/store";
import UpdateWalletNameModal
    from "../../../../common/modal/UpdateWalletNameModal/UpdateWalletNameModal";
import { type ErrorResponse, ResponseErrorType } from "../../../../common/utils/formUtils";
import { renderWithProviders } from "../../../../common/utils/test-utils";

describe("UpdateWalletNameModal", () => {
    const handlers = [
        rest.put("/api/v1/wallet/name", async (req, res, ctx) => {
            const name = req.url.searchParams.get("name");

            if (name === "error") {
                const error: ErrorResponse = {
                    type: ResponseErrorType.FIELD_VALIDATION_ERROR,
                    message: "error",
                    errorCode: "error",
                    fieldName: "name",
                };
                return await res(ctx.status(400), ctx.json([error]));
            }
            return await res(
                ctx.status(200),
                ctx.json({
                    id: 1,
                    name,
                    balance: 0,
                    currency: "USD",
                    defaultWallet: false,
                }),
            );
        }),
    ];

    const server = setupServer(...handlers);

    beforeAll(() => server.listen());
    afterEach(() => server.resetHandlers());
    afterAll(() => server.close());

    it("render", () => {
        renderWithProviders(<UpdateWalletNameModal open={true}
                                                   setOpen={jest.fn()}
                                                   walletId={1}
                                                   walletName={"test"}/>);

        expect(screen.getByText("Edit wallet name")).toBeInTheDocument();
        expect(screen.getByText("Update")).toBeInTheDocument();
    });

    it("update wallet name", async () => {
        const setOpen = jest.fn();

        const store = setupStore({
            wallets: {
                wallets: [
                    {
                        id: 1,
                        name: "test",
                        balance: 0,
                        currency: "USD",
                    },
                ],
                isInitialized: true,
            },
        });
        renderWithProviders(<UpdateWalletNameModal open={true}
                                                   setOpen={setOpen}
                                                   walletId={1}
                                                   walletName={"test"}/>, { store });

        act(() => {
            fireEvent.change(screen.getByTestId("input-name"), { target: { value: "test2" } });
        });
        await act(() => fireEvent.click(screen.getByText("Update")));
        await waitForElementToBeRemoved(() => screen.getByText("Updating..."));

        await waitFor(() => expect(store.getState().wallets.wallets[0].name).toBe("test2"));
        expect(setOpen).toBeCalledTimes(1);
    });

    it("update wallet name. Bad request", async () => {
        const setOpen = jest.fn();

        const store = setupStore({
            wallets: {
                wallets: [
                    {
                        id: 1,
                        name: "test",
                        balance: 0,
                        currency: "USD",
                    },
                ],
                isInitialized: true,
            },
        });
        renderWithProviders(<UpdateWalletNameModal open={true}
                                                   setOpen={setOpen}
                                                   walletId={1}
                                                   walletName={"test"}/>, { store });

        act(() => {
            fireEvent.change(screen.getByTestId("input-name"), { target: { value: "error" } });
        });
        await act(() => fireEvent.click(screen.getByText("Update")));

        expect(screen.getByText("Updating...")).toBeInTheDocument();

        await waitFor(() => expect(store.getState().wallets.wallets[0].name).toBe("test"));
        expect(setOpen).toBeCalledTimes(0);
    });
});
