import { describe } from "@jest/globals";
import { screen, waitFor } from "@testing-library/react";
import dayjs from "dayjs";
import { BrowserRouter } from "react-router-dom";
import App from "../../../../app/App";
import { setupStore } from "../../../../app/store";
import { renderWithProviders } from "../../../../common/utils/test-utils";
import { Role } from "../../../../features/auth/authSlice";

describe("TransactionsPage", () => {
    beforeEach(() => window.history.pushState({}, "transactions", "/transactions"));

    it("render", async () => {
        const store = setupStoreWithAuth();
        renderWithProviders(<App/>, { store, wrapper: BrowserRouter });

        // Few loaders on init.
        await waitFor(() => expect(screen.getAllByTestId("main-loader")[1]).toBeInTheDocument());
        await waitFor(() => expect(screen.getByTestId("transaction-page")).toBeInTheDocument());
        await waitFor(() => expect(screen.getByTestId("wallet-block")).toBeInTheDocument());
        await waitFor(() => expect(screen.getByTestId("transaction-block")).toBeInTheDocument());
        expect(screen.getByText("Add new transaction")).toBeInTheDocument();
        // Date block
        expect(screen.getByDisplayValue(dayjs().format("YYYY-MM-DD"))).toBeInTheDocument();
    });

    it("render. no Wallets. Do not display Add trnsaction button", async () => {
        const store = setupStoreWithAuth(false);
        renderWithProviders(<App/>, { store, wrapper: BrowserRouter });

        await waitFor(() => {
            expect(screen.getByTestId("transaction-page")).toBeInTheDocument();
        }, { timeout: 2000 });
        await waitFor(() => {
            expect(screen.queryByText("Add new transaction")).not.toBeInTheDocument();
        });
    });
});

function setupStoreWithAuth(addWallets = true) {
    const wallets = addWallets
        ? {
            wallets: [
                {
                    id: 1,
                    name: "Wallet 1",
                    balance: 100,
                    currency: "USD",
                },
            ],
            isInitialized: true,
        }
        : {
            wallets: [],
            isInitialized: true,
        };

    return setupStore({
        auth: {
            firstName: "Test",
            lastName: "Test",
            username: "Test",
            roles: [Role.ROLE_USER],
            csrfToken: "Test",
            isInitialized: true,
            isForceLogin: false,
        },
        wallets,
    });
}
