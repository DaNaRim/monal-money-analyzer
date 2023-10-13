import { describe } from "@jest/globals";
import { act, fireEvent, screen, waitForElementToBeRemoved } from "@testing-library/react";
import { rest } from "msw";
import { setupServer } from "msw/node";
import { type ErrorResponse, ResponseErrorType } from "../../../../app/hooks/formUtils";
import { setupStore } from "../../../../app/store";
import DeleteWalletModal from "../../../../common/modal/DeleteWalletModal/DeleteWalletModal";
import { renderWithProviders } from "../../../../common/utils/test-utils";

const deletableWallets = {
    wallets: [
        {
            id: 0,
            name: "Wallet 0",
            balance: 0,
            currency: "USD",
        },
    ],
    isInitialized: true,
};

const undeletableWallets = {
    wallets: [
        {
            id: 1,
            name: "Wallet 1",
            balance: 0,
            currency: "USD",
        },
    ],
    isInitialized: true,
};

describe("DeleteWalletModal", () => {
    const handlers = [
        rest.get("/api/v1/wallet/countTransactions", async (req, res, ctx) => {
            const walletId = req.url.searchParams.get("walletId");

            if (walletId === "-1") {
                return await res(ctx.status(500));
            }
            if (Number(walletId) <= 0) {
                return await res(ctx.status(200), ctx.json(0));
            }
            return await res(ctx.status(200), ctx.json(34));
        }),
        rest.delete("/api/v1/wallet", async (req, res, ctx) => {
            const walletId = req.url.searchParams.get("walletId");

            if (walletId === "-2") {
                const error: ErrorResponse = {
                    message: "Error message",
                    type: ResponseErrorType.GLOBAL_ERROR,
                    errorCode: "code",
                    fieldName: ResponseErrorType.GLOBAL_ERROR,
                };
                return await res(ctx.status(400), ctx.json([error]));
            }
            if (walletId === "-3") {
                res.networkError("Failed to connect");
                return;
            }
            return await res(ctx.status(204));
        }),
    ];

    const server = setupServer(...handlers);

    jest.useFakeTimers();

    beforeAll(() => server.listen());
    afterEach(() => server.resetHandlers());
    afterAll(() => server.close());

    it("render. Can delete", async () => {
        renderWithProviders(<DeleteWalletModal open={true} setOpen={jest.fn()} walletId={0}/>);

        expect(screen.getByText("Delete wallet?")).toBeInTheDocument();

        await waitForElementToBeRemoved(screen.getByText("Checking..."));

        expect(screen.getByText("Your wallet has no transactions. You can delete it."))
            .toBeInTheDocument();

        expect(screen.getByText("Cancel")).toBeInTheDocument();
        expect(screen.getByText("Delete")).toBeInTheDocument();
        expect(screen.queryByText("Go back")).not.toBeInTheDocument();
    });

    it("render. Cannot delete", async () => {
        renderWithProviders(<DeleteWalletModal open={true} setOpen={jest.fn()} walletId={1}/>);

        expect(screen.getByText("Delete wallet?")).toBeInTheDocument();

        await waitForElementToBeRemoved(screen.getByText("Checking..."));

        expect(screen.getByText("Your wallet has transactions (34). You can't delete it."))
            .toBeInTheDocument();

        expect(screen.getByText("Go back")).toBeInTheDocument();
        expect(screen.queryByText("Cancel")).not.toBeInTheDocument();
        expect(screen.queryByText("Delete")).not.toBeInTheDocument();
    });

    it("render. Checking error", async () => {
        renderWithProviders(<DeleteWalletModal open={true} setOpen={jest.fn()} walletId={-1}/>);

        await waitForElementToBeRemoved(screen.getByText("Checking..."));

        expect(screen.getByText("Failed to check wallet. Please try again later."))
            .toBeInTheDocument();

        expect(screen.getByText("Go back")).toBeInTheDocument();
        expect(screen.queryByText("Cancel")).not.toBeInTheDocument();
        expect(screen.queryByText("Delete")).not.toBeInTheDocument();
    });

    it("Cancel. Can delete", async () => {
        const store = setupStore({
            wallets: deletableWallets,
        });
        const setOpen = jest.fn();
        renderWithProviders(<DeleteWalletModal open={true}
                                               setOpen={setOpen}
                                               walletId={0}/>, { store });

        await waitForElementToBeRemoved(screen.getByText("Checking..."));

        await act(() => fireEvent.click(screen.getByText("Cancel")));

        expect(setOpen).toBeCalledWith(false);
        expect(store.getState().wallets.wallets).toEqual(deletableWallets.wallets);
    });

    it("Go back. Cannot delete", async () => {
        const store = setupStore({
            wallets: undeletableWallets,
        });
        const setOpen = jest.fn();
        renderWithProviders(<DeleteWalletModal open={true} setOpen={setOpen} walletId={1}/>);

        await waitForElementToBeRemoved(screen.getByText("Checking..."));

        await act(() => fireEvent.click(screen.getByText("Go back")));

        expect(setOpen).toBeCalledWith(false);
        expect(store.getState().wallets.wallets).toEqual(undeletableWallets.wallets);
    });

    it("Delete wallet", async () => {
        const store = setupStore({
            wallets: deletableWallets,
        });
        const setOpen = jest.fn();
        renderWithProviders(<DeleteWalletModal open={true}
                                               setOpen={setOpen}
                                               walletId={0}/>, { store });

        await waitForElementToBeRemoved(screen.getByText("Checking..."));

        await act(() => fireEvent.click(screen.getByText("Delete")));

        await waitForElementToBeRemoved(screen.getByText("Deleting..."));

        expect(store.getState().wallets.wallets).toEqual([]);
        expect(setOpen).toBeCalledWith(false);
    });

    it("Delete wallet. Global error", async () => {
        const store = setupStore({
            wallets: deletableWallets,
        });
        const setOpen = jest.fn();
        renderWithProviders(<DeleteWalletModal open={true}
                                               setOpen={setOpen}
                                               walletId={-2}/>, { store });

        await waitForElementToBeRemoved(screen.getByText("Checking..."));

        await act(() => fireEvent.click(screen.getByText("Delete")));

        await waitForElementToBeRemoved(screen.getByText("Deleting..."));

        expect(screen.getByText("Error message")).toBeInTheDocument();
        expect(screen.getByText("Go back")).toBeInTheDocument();
        expect(screen.queryByText("Cancel")).not.toBeInTheDocument();
        expect(screen.queryByText("Delete")).not.toBeInTheDocument();
        expect(store.getState().wallets.wallets).toEqual(deletableWallets.wallets);
        expect(setOpen).not.toBeCalled();
    });

    it("Delete wallet. Fetch error", async () => {
        const store = setupStore({
            wallets: deletableWallets,
        });
        const setOpen = jest.fn();
        renderWithProviders(<DeleteWalletModal open={true}
                                               setOpen={setOpen}
                                               walletId={-3}/>, { store });

        await waitForElementToBeRemoved(screen.getByText("Checking..."));

        await act(() => fireEvent.click(screen.getByText("Delete")));

        await waitForElementToBeRemoved(screen.getByText("Deleting..."));

        expect(screen.getByText("Server unavailable. please try again later"))
            .toBeInTheDocument();
        expect(screen.getByText("Go back")).toBeInTheDocument();
        expect(screen.queryByText("Cancel")).not.toBeInTheDocument();
        expect(screen.queryByText("Delete")).not.toBeInTheDocument();
        expect(store.getState().wallets.wallets).toEqual(deletableWallets.wallets);
        expect(setOpen).not.toBeCalled();
    });
});
