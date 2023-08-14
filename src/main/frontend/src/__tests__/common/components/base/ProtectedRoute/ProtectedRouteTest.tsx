import { describe } from "@jest/globals";
import { act, screen, waitFor } from "@testing-library/react";
import { rest } from "msw";
import { setupServer } from "msw/node";
import { BrowserRouter } from "react-router-dom";
import App from "../../../../../app/App";
import { setupStore } from "../../../../../app/store";
import { renderWithProviders } from "../../../../../common/utils/test-utils";
import { Role } from "../../../../../features/auth/authSlice";

describe("ProtectedRoute", () => {
    const handlers = [
        rest.post("api/v1/auth/getState", async (req, res, ctx) => {
            return res(ctx.status(401)); // Just to be sure
        }),
    ];
    const server = setupServer(...handlers);

    beforeAll(() => server.listen());
    beforeEach(() => window.history.pushState({}, "ProtectedRoute", "/transactions"));
    afterEach(() => server.resetHandlers());
    afterAll(() => server.close());

    it("pending auth -> loading", async () => {
        renderWithProviders(<App/>, { wrapper: BrowserRouter });

        await act(async () => await new Promise((resolve) => setTimeout(resolve, 10)));

        expect(screen.getByTestId("main-loader")).toBeInTheDocument();
        expect(screen.queryByTestId("transaction-page")).not.toBeInTheDocument();
    });

    it("no auth -> navigate to login", () => {
        const store = setupStore({
            auth: {
                firstName: null,
                lastName: null,
                username: null,
                roles: [],
                csrfToken: null,
                isInitialized: true,
                isForceLogin: false,
            },
        });
        renderWithProviders(<App/>, { store, wrapper: BrowserRouter });

        waitFor(() => {
            expect(screen.queryByTestId("transaction-page")).not.toBeInTheDocument();
            expect(screen.getByTestId("login-page")).toBeInTheDocument();
            expect(store.getState().auth.isForceLogin).toBeTruthy();
        });
    });

    it("auth without needed role -> navigate to forbidden", () => {
        const store = setupStore({
            auth: {
                firstName: "Test",
                lastName: "Test",
                username: "Test",
                roles: [],
                csrfToken: "Test",
                isInitialized: true,
                isForceLogin: false,
            },
        });
        renderWithProviders(<App/>, { store, wrapper: BrowserRouter });

        waitFor(() => {
            expect(screen.queryByTestId("transaction-page")).not.toBeInTheDocument();
            expect(screen.getByTestId("forbidden-page")).toBeInTheDocument();
            expect(store.getState().auth.isForceLogin).toBeFalsy();
        });
    });

    it("auth check success -> navigate to page", () => {
        const store = setupStore({
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
        renderWithProviders(<App/>, { store, wrapper: BrowserRouter });

        waitFor(() => expect(screen.getByTestId("transaction-page")).toBeInTheDocument());
    });
});
