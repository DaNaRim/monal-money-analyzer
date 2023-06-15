import { it } from "@jest/globals";
import { screen, waitFor } from "@testing-library/react";
import { rest } from "msw";
import { setupServer } from "msw/node";
import { BrowserRouter } from "react-router-dom";
import { setupStore } from "../../../../../app/store";
import Header from "../../../../../common/components/base/Header/Header";
import { renderWithProviders } from "../../../../../common/utils/test-utils";

const LOADING_TEXT = "Loading...";
const LOGIN_BUTTON_TEXT = "Login";
const REGISTER_BUTTON_TEXT = "Register";
const LOGOUT_BUTTON_TEXT = "Logout";

const authTestHandlers = [
    rest.post("api/v1/auth/getState", (req, res, ctx) => {
        const accessToken = req.cookies["access_token"];

        if (accessToken === undefined) {
            return res(ctx.status(401));
        }
        const authState = {
            username: "a@b.c",
            firstName: "John",
            lastName: "Smith",
            roles: ["ROLE_USER"],
            csrfToken: "1234567890",
        };
        return res(ctx.json(authState), ctx.delay(150), ctx.status(200));
    }),
    rest.post("api/v1/auth/refresh", (req, res, ctx) => {
        return res(ctx.status(401));
    }),
    rest.post("api/v1/logout", (req, res, ctx) => {
        return res(ctx.status(200));
    }),
];

describe("Header auth block", () => {

    const server = setupServer(...authTestHandlers);

    beforeAll(() => server.listen());
    afterEach(() => {
        server.resetHandlers();
        document.cookie = "authInit=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
        document.cookie = "access_token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
    });
    afterAll(() => server.close());


    it("rendered. authInit cookie NOT exists -> display auth buttons", async () => {
        const store = setupStore();
        renderWithProviders(<Header/>, { wrapper: BrowserRouter, store });

        expect(screen.queryByText(LOADING_TEXT)).toBeNull();
        expect(screen.getByText(LOGIN_BUTTON_TEXT)).toBeInTheDocument();
        expect(screen.getByText(REGISTER_BUTTON_TEXT)).toBeInTheDocument();
        expect(screen.queryByText(LOGOUT_BUTTON_TEXT)).toBeNull();

        expect(store.getState().auth.isInitialized).toBeTruthy();
    });

    it("rendered. authInit cookie exists -> display auth loading", async () => {
        document.cookie = "authInit=true";

        const store = setupStore();
        renderWithProviders(<Header/>, { wrapper: BrowserRouter, store });

        expect(screen.getByText(LOADING_TEXT)).toBeInTheDocument();
        expect(screen.queryByText(LOGIN_BUTTON_TEXT)).toBeNull();
        expect(screen.queryByText(REGISTER_BUTTON_TEXT)).toBeNull();
        expect(screen.queryByText(LOGOUT_BUTTON_TEXT)).toBeNull();

        expect(store.getState().auth.isInitialized).toBeFalsy();
    });

    it("init auth failed -> display auth buttons", async () => {
        document.cookie = "authInit=true";

        const store = setupStore();
        renderWithProviders(<Header/>, { wrapper: BrowserRouter, store });

        await waitFor(() => {
            expect(screen.queryByText(LOADING_TEXT)).toBeNull();
            expect(screen.getByText(LOGIN_BUTTON_TEXT)).toBeInTheDocument();
            expect(screen.getByText(REGISTER_BUTTON_TEXT)).toBeInTheDocument();
            expect(screen.queryByText(LOGOUT_BUTTON_TEXT)).toBeNull();

            expect(store.getState().auth.isInitialized).toBeTruthy();
        });
    });

    it("init auth success -> display name and logout button", async () => {
        document.cookie = "authInit=true";
        document.cookie = "access_token=1234567890";

        const store = setupStore();
        renderWithProviders(<Header/>, { wrapper: BrowserRouter, store });

        await waitFor(() => {
            expect(screen.queryByText(LOADING_TEXT)).toBeNull();
            expect(screen.queryByText(LOGIN_BUTTON_TEXT)).toBeNull();
            expect(screen.queryByText(REGISTER_BUTTON_TEXT)).toBeNull();
            expect(screen.getByText("John Smith")).toBeInTheDocument();
            expect(screen.getByText(LOGOUT_BUTTON_TEXT)).toBeInTheDocument();

            expect(store.getState().auth.isInitialized).toBeTruthy();
        });
    });

    it("click logout button -> logout", async () => {
        document.cookie = "authInit=true";
        document.cookie = `access_token=1234567890`;

        const store = setupStore();
        renderWithProviders(<Header/>, { wrapper: BrowserRouter, store });

        await waitFor(() => screen.getByText(LOGOUT_BUTTON_TEXT).click());

        await waitFor(() => {
            expect(screen.queryByText("John Smith")).toBeNull();
            expect(screen.getByText(LOGIN_BUTTON_TEXT)).toBeInTheDocument();
            expect(screen.getByText(REGISTER_BUTTON_TEXT)).toBeInTheDocument();

            expect(store.getState().auth.isInitialized).toBeTruthy();
        });
    });
});
