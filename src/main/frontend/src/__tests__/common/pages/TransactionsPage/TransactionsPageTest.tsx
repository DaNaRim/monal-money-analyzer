import { describe } from "@jest/globals";
import { screen, waitFor } from "@testing-library/react";
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
    });
});

function setupStoreWithAuth() {
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
    });
}
