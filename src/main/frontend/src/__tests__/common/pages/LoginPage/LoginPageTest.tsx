import { describe } from "@jest/globals";
import { act, fireEvent, screen, waitFor, waitForElementToBeRemoved } from "@testing-library/react";
import { rest } from "msw";
import { setupServer } from "msw/node";
import React from "react";
import { BrowserRouter } from "react-router-dom";
import App from "../../../../app/App";
import { type ErrorResponse, ResponseErrorType } from "../../../../app/hooks/formUtils";
import { setupStore } from "../../../../app/store";
import { renderWithProviders } from "../../../../common/utils/test-utils";
import {
    type AppMessage,
    AppMessageCode,
    AppMessageType,
} from "../../../../features/appMessages/appMessagesSlice";

// Test login page and form only.
// For testing auth state see HeaderTest.tsx
// For testing auth refresh logic see apiSliceTest.tsx

export const loginTestHandlers = [
    rest.post("api/v1/login", async (req, res, ctx) => {
        const { username } = await req.json();

        if (username === "Error") {
            const error: ErrorResponse = {
                message: "Invalid username or password", // Not real. Just Example
                type: ResponseErrorType.GLOBAL_ERROR,
                errorCode: "code",
                fieldName: ResponseErrorType.GLOBAL_ERROR,
            };
            return await res(ctx.status(401), ctx.json([error]));
        }
        if (username === "serverError") {
            const error: ErrorResponse = {
                message: "Server error",
                type: ResponseErrorType.SERVER_ERROR,
                errorCode: "code",
                fieldName: ResponseErrorType.SERVER_ERROR,
            };
            return await res(ctx.status(500), ctx.json([error]));
        }
        if (username === "emailError") {
            const error: ErrorResponse = {
                message: "Email error",
                type: ResponseErrorType.FIELD_VALIDATION_ERROR,
                errorCode: "code",
                fieldName: "username",
            };
            return await res(ctx.status(400), ctx.json([error]));
        }
        if (username === "disabledUserError") {
            const error: ErrorResponse = {
                message: "Account is disabled",
                type: ResponseErrorType.GLOBAL_ERROR,
                errorCode: "validation.auth.disabled",
                fieldName: ResponseErrorType.GLOBAL_ERROR,
            };
            return await res(ctx.status(400), ctx.json([error]));
        }
        return await res(ctx.status(200), ctx.json({
            username: "John",
            firstName: "John",
            lastName: "Smith",
            roles: ["ROLE_USER"],
            csrfToken: "1234567890",
        }));
    }),
    rest.post("api/v1/auth/getState", async (req, res, ctx) => {
        const accessToken = req.cookies.access_token;

        if (accessToken === undefined) {
            return await res(ctx.status(401));
        }
        const authState = {
            username: "a@b.c",
            firstName: "John",
            lastName: "Smith",
            roles: ["ROLE_USER"],
            csrfToken: "1234567890",
        };
        return await res(ctx.json(authState), ctx.delay(150), ctx.status(200));
    }),
    rest.post("api/v1/auth/refresh", async (req, res, ctx) => await res(ctx.status(401))),
    rest.post("api/v1/logout", async (req, res, ctx) => await res(ctx.status(200))),
];

describe("LoginPage", () => {
    const server = setupServer(...loginTestHandlers);

    beforeAll(() => server.listen());
    beforeEach(() => window.history.pushState({}, "Login", "/login"));
    afterEach(() => server.resetHandlers());
    afterAll(() => server.close());

    it("render", async () => {
        renderWithProviders(<App/>, { wrapper: BrowserRouter });

        await waitForElementToBeRemoved(() => screen.getByText("Loading..."));

        await waitFor(() => {
            expect(screen.getByTestId("main-header")).toBeInTheDocument();
            expect(screen.getByTestId("main-footer")).toBeInTheDocument();
            expect(screen.getByTestId("login-page")).toBeInTheDocument();

            expect(screen.getByText("Email")).toBeInTheDocument();
            expect(screen.getByText("Password")).toBeInTheDocument();
            expect(screen.getAllByText("Login").find(el => el.tagName === "BUTTON"))
                .toBeInTheDocument();
            expect(screen.getByText("Forgot password?")).toBeInTheDocument();
        });
    });

    // I don't know why but this test passes or fails randomly
    // It looks like button is not clicked sometimes
    it("fields not filled -> display required error messages", async () => {
        renderWithProviders(<App/>, { wrapper: BrowserRouter });

        await waitFor(() => clickLoginButton());

        await waitFor(() => {
            expect(screen.getByText("Email is required")).toBeInTheDocument();
            expect(screen.getByText("Password is required")).toBeInTheDocument();
        });
    });

    it("login success -> navigate to home page", async () => {
        renderWithProviders(<App/>, { wrapper: BrowserRouter });

        await waitFor(() => fillLoginInputs("123", "123"));
        act(() => clickLoginButton());

        // should display loading message
        await waitFor(() => expect(screen.getByText("Logging in...")).toBeInTheDocument());

        // should redirect to home page
        await waitFor(() => {
            expect(screen.getByTestId("home-page")).toBeInTheDocument();
            expect(screen.getByText("John Smith")).toBeInTheDocument(); // header username
        });
    });

    it("login field error -> display error message", async () => {
        renderWithProviders(<App/>, { wrapper: BrowserRouter });

        await waitFor(() => fillLoginInputs("emailError", "123"));
        act(() => clickLoginButton());

        // should display error message
        await waitFor(() => expect(screen.getByTestId("error-username")).toHaveTextContent("Email error"));
    });

    it("login global error -> display error message", async () => {
        renderWithProviders(<App/>, { wrapper: BrowserRouter });

        await waitFor(() => fillLoginInputs("Error", "123"));
        act(() => clickLoginButton());

        // should display error message
        await waitFor(() =>
            expect(screen.getByTestId("global-error"))
                .toHaveTextContent("Invalid username or password"));
    });

    it("login server error -> display error message", async () => {
        renderWithProviders(<App/>, { wrapper: BrowserRouter });

        await waitFor(() => fillLoginInputs("serverError", "123"));
        act(() => clickLoginButton());

        // should display error message
        await waitFor(() =>
            expect(screen.getByTestId("server-error"))
                .toHaveTextContent("Server error. Please try again later. If the problem"
                    + " persists, please contact the administrator"));
    });

    it("login account disabled error -> suggest resent ver. email", async () => {
        renderWithProviders(<App/>, { wrapper: BrowserRouter });

        await waitFor(() => fillLoginInputs("disabledUserError", "123"));
        act(() => clickLoginButton());

        // should display link to resend verification token
        await waitFor(() => expect(screen.getByText("Resend verification email")).toBeInTheDocument());
    });

    it("app message exists -> display message", async () => {
        const store = setupStoreWithAppMessage({
            type: AppMessageType.WARNING,
            messageCode: AppMessageCode.REGISTRATION_CONFIRMATION_SUCCESS,
            page: "login",
        });
        renderWithProviders(<App/>, { wrapper: BrowserRouter, store });

        await waitFor(() =>
            expect(screen.getByText("Account activated successfully. You can now log in."))
                .toBeInTheDocument());
    });

    it("app message acc. not verified (token not found) -> suggest resent ver. email", async () => {
        const store = setupStoreWithAppMessage({
            type: AppMessageType.WARNING,
            messageCode: AppMessageCode.TOKEN_VERIFICATION_NOT_FOUND,
            page: "login",
        });
        renderWithProviders(<App/>, { wrapper: BrowserRouter, store });

        await waitFor(() => expect(screen.getByText("Resend verification email")).toBeInTheDocument());
    });

    it("app message acc. not verified (token expired) -> suggest resent ver. email", async () => {
        const store = setupStoreWithAppMessage({
            type: AppMessageType.WARNING,
            messageCode: AppMessageCode.TOKEN_VERIFICATION_EXPIRED,
            page: "login",
        });
        renderWithProviders(<App/>, { wrapper: BrowserRouter, store });

        await waitFor(() => expect(screen.getByText("Resend verification email")).toBeInTheDocument());
    });

    it("app message & login success -> delete message", async () => {
        const store = setupStoreWithAppMessage({
            type: AppMessageType.INFO,
            messageCode: AppMessageCode.REGISTRATION_CONFIRMATION_SUCCESS,
            page: "login",
        });
        renderWithProviders(<App/>, { wrapper: BrowserRouter, store });

        await waitFor(() => fillLoginInputs("123", "123"));
        act(() => clickLoginButton());

        // app message should be deleted
        await waitFor(() => expect(store.getState().appMessages.messages).toHaveLength(0));
    });

    it("app message & login fail -> message still exists", async () => {
        const store = setupStoreWithAppMessage({
            type: AppMessageType.INFO,
            messageCode: AppMessageCode.REGISTRATION_CONFIRMATION_SUCCESS,
            page: "login",
        });
        renderWithProviders(<App/>, { wrapper: BrowserRouter, store });

        await waitFor(() => fillLoginInputs("Error", "123"));
        act(() => clickLoginButton());

        // app message should still exist
        await waitFor(() => {
            expect(screen.getByText("Account activated successfully. You can now log in."))
                .toBeInTheDocument();
            expect(store.getState().appMessages.messages).toHaveLength(1);
        });
    });
});

function fillLoginInputs(username: string, password: string) {
    const emailInput = screen.getByTestId("input-username");
    const passwordInput = screen.getByTestId("input-password");

    if (emailInput == null || passwordInput == null) {
        throw new Error("Login inputs not found");
    }
    fireEvent.change(emailInput, { target: { value: username } });
    fireEvent.change(passwordInput, { target: { value: password } });
}

function clickLoginButton() {
    const loginButton = screen.getAllByText("Login")
        .find(el => el.tagName === "BUTTON") as HTMLButtonElement;

    if (loginButton == null) {
        throw new Error("Login button not found");
    }
    loginButton.click();
}

function setupStoreWithAppMessage(appMessage: AppMessage) {
    return setupStore({
        appMessages: {
            messages: [appMessage],
        },
    });
}
