import { describe } from "@jest/globals";
import { act, fireEvent, screen, waitFor, waitForElementToBeRemoved } from "@testing-library/react";
import { rest } from "msw";
import { setupServer } from "msw/node";
import React from "react";
import { BrowserRouter } from "react-router-dom";
import App from "../../../app/App";
import { type ErrorResponse, ResponseErrorType } from "../../../common/utils/formUtils";
import { getStateHandler, renderWithProviders } from "../../../common/utils/test-utils";

const handlers = [
    rest.post("api/v1/resendVerificationToken", async (req, res, ctx) => {
        const email = req.url.searchParams.get("email");

        if (email === "400") {
            const errorMessage1: ErrorResponse = {
                message: "Bad request1",
                errorCode: "validation.user.email.invalid",
                type: ResponseErrorType.FIELD_VALIDATION_ERROR,
                fieldName: "email",
            };
            const errorMessage2: ErrorResponse = {
                message: "Bad request2",
                errorCode: "validation.user.already_verified",
                type: ResponseErrorType.GLOBAL_ERROR,
                fieldName: ResponseErrorType.GLOBAL_ERROR,
            };
            return await res(ctx.status(400), ctx.json([errorMessage1, errorMessage2]));
        }
        if (email === "fetch error") {
            res.networkError("Failed to connect");
        }
        if (email === "unknown code") {
            const errorMessage: ErrorResponse = {
                message: "Bad code",
                errorCode: "code321",
                type: ResponseErrorType.GLOBAL_ERROR,
                fieldName: ResponseErrorType.GLOBAL_ERROR,
            };
            return await res(ctx.status(400), ctx.json([errorMessage]));
        }
        if (email === "server error") {
            return await res(ctx.status(500));
        }
        if (email === "unknown error") {
            // Also, it can be some error during fetch in frontend
            return await res(ctx.status(501));
        }
        return await res(ctx.status(200));
    }),
    rest.post("api/v1/login", async (req, res, ctx) => {
        const errorMessage1: ErrorResponse = {
            message: "Bad request1",
            errorCode: "validation.user.email.invalid",
            type: ResponseErrorType.FIELD_VALIDATION_ERROR,
            fieldName: "username",
        };
        const errorMessage2: ErrorResponse = {
            message: "Bad request2",
            errorCode: "validation.user.already_verified",
            type: ResponseErrorType.GLOBAL_ERROR,
            fieldName: ResponseErrorType.GLOBAL_ERROR,
        };
        return await res(ctx.status(401), ctx.json([errorMessage1, errorMessage2]));
    }),
    getStateHandler, // disable unhandled request warning
];

describe("formUtils", () => {
    const server = setupServer(...handlers);

    beforeAll(() => server.listen());
    beforeEach(() => {
        // Use thigh path because one input only
        window.history.pushState({}, "Resend ver. token", "/resendVerificationToken");
    });
    afterEach(() => server.resetHandlers());
    afterAll(() => server.close());

    it("server 400 error -> handle errors", async () => {
        renderWithProviders(<App/>, { wrapper: BrowserRouter });

        await waitFor(() => fillResendInput("400"));
        act(() => clickSendButton());

        await waitFor(() => {
            expect(screen.getByText("Email must be a valid email address")).toBeInTheDocument();
            expect(screen.getByText("User with this email has already been verified"))
                .toBeInTheDocument();
        });
    });

    it("server 401 error -> handle errors", async () => {
        // We need login page to test 401 error. Otherwise, app will try to refresh auth state
        window.history.pushState({}, "Login", "/login");
        renderWithProviders(<App/>, { wrapper: BrowserRouter });

        await waitForElementToBeRemoved(() => screen.getByTestId("auth-loader"));

        await waitFor(() => fillLoginInputs("401", "401"));
        act(() => clickLoginButton());

        await waitFor(() => {
            expect(screen.getByText("Email must be a valid email address")).toBeInTheDocument();
            expect(screen.getByText("User with this email has already been verified"))
                .toBeInTheDocument();
        });
    });

    it("fetch error -> display server unavailable", async () => {
        renderWithProviders(<App/>, { wrapper: BrowserRouter });

        await waitFor(() => fillResendInput("fetch error"));
        act(() => clickSendButton());

        await waitFor(() => {
            expect(screen.getByText("Server unavailable. please try again later"))
                .toBeInTheDocument();
        });
    });

    it("server error -> display server error", async () => {
        renderWithProviders(<App/>, { wrapper: BrowserRouter });

        await waitFor(() => fillResendInput("server error"));
        act(() => clickSendButton());

        await waitFor(() => {
            expect(screen.getByText("Server error. Please try again later. If the problem persists,"
                + " please contact the administrator")).toBeInTheDocument();
        });
    });

    it("unknown error -> display unknown error", async () => {
        // Also it can be some error during fetch in frontend
        renderWithProviders(<App/>, { wrapper: BrowserRouter });

        await waitFor(() => fillResendInput("unknown error"));
        act(() => clickSendButton());

        await waitFor(() => {
            expect(screen.getByText("Unknown error. Please try again later. If the problem "
                + "persists, please contact the administrator")).toBeInTheDocument();
        });
    });

    it("unresolved code -> display default message", async () => {
        renderWithProviders(<App/>, { wrapper: BrowserRouter });

        await waitFor(() => fillResendInput("unknown code"));
        act(() => clickSendButton());

        await waitFor(() => {
            expect(screen.getByText(
                "Unknown error. Please contact the administrator. Error code: code321",
            )).toBeInTheDocument();
        });
    });
});

function fillResendInput(email: string) {
    const emailInput = screen.getByTestId("input-email");

    if (emailInput == null) {
        throw new Error("ApiSlice ResendVerificationToken input not found");
    }
    fireEvent.change(emailInput, { target: { value: email } });
}

function clickSendButton() {
    const sendButton = screen.getByText("Send");
    if (sendButton == null) {
        throw new Error("ApiSlice ResendVerificationToken send button not found");
    }
    sendButton.click();
}

function fillLoginInputs(username: string, password: string) {
    const emailInput = screen.getByTestId("input-username");
    const passwordInput = screen.getByTestId("input-password");

    if (emailInput == null || passwordInput == null) {
        throw new Error("ApiSlice Login inputs not found");
    }
    fireEvent.change(emailInput, { target: { value: username } });
    fireEvent.change(passwordInput, { target: { value: password } });
}

function clickLoginButton() {
    const loginButton = screen.getAllByText("Login")
        .find(el => el.tagName === "BUTTON") as HTMLButtonElement;

    if (loginButton == null) {
        throw new Error("ApiSlice Login button not found");
    }
    loginButton.click();
}
