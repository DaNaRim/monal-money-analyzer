import { describe } from "@jest/globals";
import { screen, waitFor } from "@testing-library/react";
import React from "react";
import { BrowserRouter } from "react-router-dom";
import App from "../../../../../app/App";
import { renderWithProviders } from "../../../../../common/utils/test-utils";

describe("ErrorPage", () => {
    beforeEach(() => window.history.pushState({}, "Test Title", "/error"));

    it("render", async () => {
        renderWithProviders(<App/>, { wrapper: BrowserRouter });

        expect(screen.getByTestId("main-header")).toBeInTheDocument();
        expect(screen.getByTestId("main-footer")).toBeInTheDocument();

        expect(screen.getByTestId("main-loader")).toBeInTheDocument();

        await waitFor(() => expect(screen.getByTestId("error-page")).toBeInTheDocument());
    });
});
