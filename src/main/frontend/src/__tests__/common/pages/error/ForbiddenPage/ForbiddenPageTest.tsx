import { describe } from "@jest/globals";
import { screen, waitFor } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";
import App from "../../../../../app/App";
import { renderWithProviders } from "../../../../../common/utils/test-utils";

describe("ForbiddenPage", () => {

    beforeEach(() => window.history.pushState({}, "Test Title", "/forbidden"));

    it("render", async () => {
        renderWithProviders(<App />, { wrapper: BrowserRouter});

        expect(screen.getByTestId("main-header")).toBeInTheDocument();
        expect(screen.getByTestId("main-footer")).toBeInTheDocument();

        expect(screen.getByText("Loading...")).toBeInTheDocument();

        await waitFor(() => expect(screen.getByTestId("forbidden-page")).toBeInTheDocument());
    });
});
