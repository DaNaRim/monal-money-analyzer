import { describe } from "@jest/globals";
import { act, screen, waitFor } from "@testing-library/react";
import dayjs from "dayjs";
import { BrowserRouter } from "react-router-dom";
import App from "../../../../app/App";
import { renderWithProviders, setupStoreWithAuth } from "../../../../common/utils/test-utils";

describe("TransactionsPage", () => {
    beforeEach(() => window.history.pushState({}, "transactions", "/transactions"));

    it("render", async () => {
        const store = setupStoreWithAuth({
            wallets: {
                wallets: [
                    {
                        id: 1,
                        name: "Wallet 1",
                        balance: 100,
                        currency: "USD",
                    },
                ],
                isInitialized: true,
            },
        });
        renderWithProviders(<App/>, { store, wrapper: BrowserRouter });

        // Few loaders on init.
        await waitFor(() => expect(screen.getAllByTestId("main-loader")[1]).toBeInTheDocument());

        await act(async () => await new Promise((resolve) => setTimeout(resolve, 500)));

        await waitFor(() => expect(screen.getByTestId("transaction-page")).toBeInTheDocument());
        await waitFor(() => expect(screen.getByTestId("wallet-block")).toBeInTheDocument());
        await waitFor(() => expect(screen.getByTestId("transaction-block")).toBeInTheDocument());
        expect(screen.getByText("Add new transaction")).toBeInTheDocument();
        // Date block
        expect(screen.getByDisplayValue(dayjs().format("YYYY-MM-DD"))).toBeInTheDocument();
    }, 20_000);

    it("render. no Wallets. Do not display Add transaction button", async () => {
        const store = setupStoreWithAuth();
        renderWithProviders(<App/>, { store, wrapper: BrowserRouter });

        await waitFor(() => {
            expect(screen.getByTestId("transaction-page")).toBeInTheDocument();
        }, { timeout: 3000 });
        await waitFor(() => {
            expect(screen.queryByText("Add new transaction")).not.toBeInTheDocument();
        });
    });
});
