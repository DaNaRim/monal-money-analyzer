import { describe } from "@jest/globals";
import { fireEvent, render, screen, waitFor } from "@testing-library/react";
import React from "react";
import { BrowserRouter } from "react-router-dom";
import App from "../../../../../app/App";
import LanguageContextProvider from "../../../../../app/contexts/LanguageContext";
import LanguageHandler from "../../../../../common/components/base/LanguageHandler/LanguageHandler";
import { renderWithProviders } from "../../../../../common/utils/test-utils";

describe("LanguageHandler", () => {

    beforeEach(() => {
        Object.defineProperty(document, "cookie", {
            writable: true,
            value: "", // empty cookie on start
        });
    });

    afterEach(() => {
        document.cookie = "locale=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
    });

    test("rendered", () => {
        render(<LanguageHandler/>, { wrapper: LanguageContextProvider });

        expect(screen.getByText("English")).toBeInTheDocument();
        expect(screen.getByText("Українська")).toBeInTheDocument();

        expect(getLocaleFromCookie()).toEqual("en"); //default value
    });

    test("rendered. locale cookie exists", () => {
        document.cookie = "locale=uk";

        render(<LanguageHandler/>, { wrapper: LanguageContextProvider });

        expect(screen.getByText("English")).toBeInTheDocument();
        expect(screen.getByText("Українська")).toBeInTheDocument();

        expect(getLocaleFromCookie()).toEqual("uk");
    });

    test("change language -> cookie updates", () => {
        render(<LanguageHandler/>, { wrapper: LanguageContextProvider });

        fireEvent.change(screen.getByTestId("language-handler"), {
            target: { value: "uk" },
        });
        expect(getLocaleFromCookie()).toEqual("uk");
    });

    it("change language -> page language updates", () => {
        renderWithProviders(<App/>, { wrapper: RouterAndLanguageWrapper });

        waitFor(() => {
            expect(screen.getByText("Home Page")).toBeInTheDocument();
        });
        fireEvent.change(screen.getByTestId("language-handler"), {
            target: { value: "uk" },
        });
        waitFor(() => {
            expect(screen.getByText("Домашня сторінка")).toBeInTheDocument();
        });
    });
});

const RouterAndLanguageWrapper = ({ children }: { children: React.ReactNode }) => (
    <LanguageContextProvider>
        <BrowserRouter>
            {children}
        </BrowserRouter>
    </LanguageContextProvider>
);

function getLocaleFromCookie() {
    return document.cookie
        .split("; ")
        .find((row) => row.startsWith("locale="))
        ?.split("=")[1];
}
