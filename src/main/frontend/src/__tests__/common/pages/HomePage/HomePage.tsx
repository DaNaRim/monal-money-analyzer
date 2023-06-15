import { describe } from "@jest/globals";
import { screen } from "@testing-library/dom";
import { waitFor } from "@testing-library/react";
import { BrowserRouter } from "react-router-dom";
import App from "../../../../app/App";
import { renderWithProviders } from "../../../../common/utils/test-utils";

describe("HomePage", () => {

    beforeEach(() => window.history.pushState({}, "Home", "/"));

    it("render", async () => {
        renderWithProviders(<App />, {wrapper: BrowserRouter});

        expect(screen.getByTestId("main-header")).toBeInTheDocument();
        expect(screen.getByTestId("main-footer")).toBeInTheDocument();

        expect(screen.getByText("Loading...")).toBeInTheDocument();

        await waitFor(() => expect(screen.getByTestId("home-page")).toBeInTheDocument());
    });

});
