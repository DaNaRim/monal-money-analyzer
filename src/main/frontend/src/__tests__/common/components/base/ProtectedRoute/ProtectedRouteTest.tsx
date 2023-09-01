import { describe } from "@jest/globals";
import { screen, waitFor } from "@testing-library/react";
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
            return await res(ctx.status(401)); // Just to be sure
        }),
        rest.post("api/v1/auth/refresh", async (req, res, ctx) => {
            return await res(ctx.status(401)); // Just to be sure
        }),
    ];
    const server = setupServer(...handlers);

    beforeAll(() => server.listen());
    beforeEach(() => window.history.pushState({}, "ProtectedRoute", "/transactions"));
    afterEach(() => server.resetHandlers());
    afterAll(() => server.close());

    it("pending auth -> display stub", async () => {
        renderWithProviders(<App/>, { wrapper: BrowserRouter });

        expect(screen.getByTestId("main-loader")).toBeInTheDocument();
        expect(screen.queryByTestId("transaction-page")).not.toBeInTheDocument();
    });

    it("no auth -> navigate to login", async () => {
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

        await waitFor(() => {
            expect(screen.queryByTestId("transaction-page")).not.toBeInTheDocument();
            expect(screen.getByTestId("login-page")).toBeInTheDocument();
            expect(store.getState().auth.isForceLogin).toBeTruthy();
        });
    });

    it("auth without needed role -> navigate to forbidden", async () => {
        const store = setupStore({
            auth: {
                firstName: "Test",
                lastName: "Test",
                username: "Test",
                roles: [Role.ROLE_ADMIN],
                csrfToken: "Test",
                isInitialized: true,
                isForceLogin: false,
            },
        });
        renderWithProviders(<App/>, { store, wrapper: BrowserRouter });

        await waitFor(() => {
            expect(screen.queryByTestId("transaction-page")).not.toBeInTheDocument();
            expect(screen.getByTestId("forbidden-page")).toBeInTheDocument();
            expect(store.getState().auth.isForceLogin).toBeFalsy();
        });
    });

    it("auth check success -> navigate to page", async () => {
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

        const loaders = screen.getAllByTestId("main-loader");

        await waitFor(() => {
            // There is a moment when both loaders (from ProtectedRoute and from PageWrapper)
            // are in the DOM but only one is visible
            expect(loaders[0]).toBeInTheDocument();
            expect(loaders[0]).toHaveStyle("display: none");
            expect(loaders[1]).toBeInTheDocument();
        });
        await waitFor(() => expect(screen.getByTestId("transaction-page")).toBeInTheDocument());
    }, 10_000);
});
