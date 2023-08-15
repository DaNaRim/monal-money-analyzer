import { describe } from "@jest/globals";
import { screen, waitFor } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";
import App from "../../../../app/App";
import { renderWithProviders } from "../../../../common/utils/test-utils";

describe("TransactionsPage", () => {
    beforeEach(() => window.history.pushState({}, "transactions", "/transactions"));

    it("render", () => {
        renderWithProviders(<App/>, { wrapper: BrowserRouter });

        waitFor(() => screen.getByTestId("transaction-page"));
    });
});