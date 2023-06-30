import { describe, it } from "@jest/globals";
import { fireEvent, screen, waitFor } from "@testing-library/react";
import { rest } from "msw";
import { setupServer } from "msw/node";
import React from "react";
import { BrowserRouter } from "react-router-dom";
import App from "../../../../app/App";
import { type ErrorResponse, ResponseErrorType } from "../../../../app/hooks/formUtils";
import { getStateHandler, renderWithProviders } from "../../../../common/utils/test-utils";

describe("ResendVerificationTokenPage", () => {
    const handlers = [
        rest.post("api/v1/resendVerificationToken", async (req, res, ctx) => {
            const email = req.url.searchParams.get("email");

            if (email === "Email error") {
                const error: ErrorResponse = {
                    message: "Email error",
                    type: ResponseErrorType.FIELD_VALIDATION_ERROR,
                    errorCode: "code",
                    fieldName: "email",
                };
                return await res(ctx.status(400), ctx.json([error]));
            }
            if (email === "Global error") {
                const error: ErrorResponse = {
                    message: "Global error",
                    type: ResponseErrorType.GLOBAL_ERROR,
                    errorCode: "code",
                    fieldName: ResponseErrorType.GLOBAL_ERROR,
                };
                return await res(ctx.status(400), ctx.json([error]));
            }
            if (email === "Server error") {
                const error: ErrorResponse = {
                    message: "Server error",
                    type: ResponseErrorType.SERVER_ERROR,
                    errorCode: "code",
                    fieldName: ResponseErrorType.SERVER_ERROR,
                };
                return await res(ctx.status(500), ctx.json([error]));
            }
            return await res(ctx.status(200));
        }),
        getStateHandler, // disable unhandled request warning
    ];

    const server = setupServer(...handlers);

    beforeAll(() => server.listen());
    beforeEach(() => window.history.pushState({}, "Resend ver. token", "/resendVerificationToken"));
    afterEach(() => server.resetHandlers());
    afterAll(() => server.close());

    it("render", async () => {
        renderWithProviders(<App/>, { wrapper: BrowserRouter });

        expect(screen.getByTestId("main-loader")).toBeInTheDocument();

        await waitFor(() => {
            expect(screen.getByTestId("main-header")).toBeInTheDocument();
            expect(screen.getByTestId("main-footer")).toBeInTheDocument();
            expect(screen.getByTestId("resend-verification-token-page")).toBeInTheDocument();

            expect(screen.getByText("Email")).toBeInTheDocument();
            expect(screen.getByText("Send")).toBeInTheDocument();
        });
    });

    // I don't know why but this test passes or fails randomly
    // It looks like button is not clicked sometimes
    it("fields not filled -> display required error messages", async () => {
        renderWithProviders(<App/>, { wrapper: BrowserRouter });

        await waitFor(() => clickSendButton());

        await waitFor(() => {
            expect(screen.getByText("Email is required")).toBeInTheDocument();
            expect(screen.getByText("Send")).toBeInTheDocument();
        });
    });

    it("resend success -> display success message", async () => {
        renderWithProviders(<App/>, { wrapper: BrowserRouter });

        await waitFor(() => fillResendInput("a@b"));
        clickSendButton();

        await waitFor(() => expect(screen.getByText("Processing...")).toBeInTheDocument());

        await waitFor(async () => {
            expect(screen.getByText("Verification email sent."
                + " Please check your email to activate your account."
                + " If it doesn't appear within a few minutes, check your spam folder."))
                .toBeInTheDocument();
            expect(screen.getByText("Send")).toBeInTheDocument(); // button is disabled again
        });
    });

    it("resend field error -> display error message", async () => {
        renderWithProviders(<App/>, { wrapper: BrowserRouter });

        await waitFor(() => fillResendInput("Email error"));
        clickSendButton();

        // should display error message
        await waitFor(() => {
            expect(screen.getByTestId("error-email")).toHaveTextContent("Email error");
        });
    });

    it("resend global error -> display error message", async () => {
        renderWithProviders(<App/>, { wrapper: BrowserRouter });

        await waitFor(() => fillResendInput("Global error"));
        clickSendButton();

        // should display error message
        await waitFor(() =>
            expect(screen.getByTestId("global-error"))
                .toHaveTextContent("Global error"));
    });

    it("resend server error -> display error message", async () => {
        renderWithProviders(<App/>, { wrapper: BrowserRouter });

        await waitFor(() => fillResendInput("Server error"));
        clickSendButton();

        // should display error message
        await waitFor(() =>
            expect(screen.getByTestId("server-error"))
                .toHaveTextContent("Server error. Please try again later. If the problem"
                    + " persists, please contact the administrator"));
    });
});

export function fillResendInput(email: string) {
    const emailInput = screen.getByTestId("input-email");
    if (emailInput == null) {
        throw new Error("ResendVerificationToken input not found");
    }
    fireEvent.change(emailInput, { target: { value: email } });
}

export function clickSendButton() {
    const sendButton = screen.getByText("Send");
    if (sendButton == null) {
        throw new Error("ResendVerificationToken send button not found");
    }
    sendButton.click();
}
