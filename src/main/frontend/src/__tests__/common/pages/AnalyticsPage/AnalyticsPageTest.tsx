import { describe } from "@jest/globals";
import { screen, waitFor } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";
import App from "../../../../app/App";
import { renderWithProviders, setupStoreWithAuth } from "../../../../common/utils/test-utils";

describe("AnalyticsPage", () => {
    beforeEach(() => window.history.pushState({}, "analytics", "/analytics"));

    it("render", async () => {
        const store = setupStoreWithAuth();
        renderWithProviders(<App/>, { store, wrapper: BrowserRouter });

        // Few loaders on init.
        await waitFor(() => expect(screen.getAllByTestId("main-loader")[1]).toBeInTheDocument());

        await waitFor(() => {
            expect(screen.getByTestId("analytics-page")).toBeInTheDocument();
            expect(screen.getByTestId("wallet-block")).toBeInTheDocument();
            expect(screen.getByTestId("analytics-bar")).toBeInTheDocument();
        });
    }, 10_000);
});
