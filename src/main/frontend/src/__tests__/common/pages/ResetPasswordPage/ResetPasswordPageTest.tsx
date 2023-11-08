import { describe, it } from "@jest/globals";
import { fireEvent, screen, waitFor } from "@testing-library/react";
import { rest } from "msw";
import { setupServer } from "msw/node";
import React from "react";
import { BrowserRouter } from "react-router-dom";
import App from "../../../../app/App";
import { type ErrorResponse, ResponseErrorType } from "../../../../common/utils/formUtils";
import { getStateHandler, renderWithProviders } from "../../../../common/utils/test-utils";

describe("RegistrationPage", () => {
    const handlers = [
        rest.post("api/v1/resetPassword", async (req, res, ctx) => {
            const email = req.url.searchParams.get("email");

            if (email === "Email error") {
                const error: ErrorResponse = {
                    message: "Email error",
                    type: ResponseErrorType.FIELD_VALIDATION_ERROR,
                    errorCode: "validation.user.email.invalid",
                    fieldName: "email",
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
    beforeEach(() => window.history.pushState({}, "Reset password page", "/resetPassword"));
    afterEach(() => server.resetHandlers());
    afterAll(() => server.close());

    it("render", async () => {
        renderWithProviders(<App/>, { wrapper: BrowserRouter });

        expect(screen.getByTestId("main-loader")).toBeInTheDocument();

        await waitFor(() => {
            expect(screen.getByTestId("main-header")).toBeInTheDocument();
            expect(screen.getByTestId("main-footer")).toBeInTheDocument();
            expect(screen.getByTestId("reset-password-page")).toBeInTheDocument();

            expect(screen.getByText("Email")).toBeInTheDocument();
            expect(screen.getByText("Send")).toBeInTheDocument();
        }, { timeout: 10_000, interval: 1000 });
    });

    // I don't know why but this test passes or fails randomly
    // It looks like button is not clicked sometimes
    it("fields not filled -> display required error messages", async () => {
        renderWithProviders(<App/>, { wrapper: BrowserRouter });

        await waitFor(() => clickSendButton());

        await waitFor(() => expect(screen.getByText("Email is required")).toBeInTheDocument());
    });

    it("reset success -> display success message", async () => {
        renderWithProviders(<App/>, { wrapper: BrowserRouter });

        await waitFor(() => fillPasswordResetInput("a@b"));

        clickSendButton();

        await waitFor(() => expect(screen.getByText("Processing...")).toBeInTheDocument());

        // To disable act warning. I don't know how to fix it
        await waitFor(async () => await new Promise(resolve => setTimeout(resolve, 100)));

        await waitFor(() => {
            expect(screen.getByText("Check your email for a link to reset your password."
                + " If it doesn't appear within a few minutes, check your spam folder."))
                .toBeInTheDocument();
            expect(screen.getByText("Send")).toBeInTheDocument(); // button is disabled again
        });
    });

    it("reset field error -> display error message", async () => {
        renderWithProviders(<App/>, { wrapper: BrowserRouter });

        await waitFor(() => fillPasswordResetInput("Email error"));

        clickSendButton();

        // should display error message
        await waitFor(() => {
            expect(screen.getByTestId("error-email"))
                .toHaveTextContent("Email must be a valid email address");
        });
    });

    it("reset server error -> display error message", async () => {
        renderWithProviders(<App/>, { wrapper: BrowserRouter });

        await waitFor(() => fillPasswordResetInput("Server error"));
        clickSendButton();

        // should display error message
        await waitFor(() =>
            expect(screen.getByTestId("server-error-message"))
                .toHaveTextContent("Server error. Please try again later. If the problem"
                    + " persists, please contact the administrator"));
    });
});

function fillPasswordResetInput(email: string) {
    const emailInput = screen.getByTestId("input-email");

    if (emailInput == null) {
        throw new Error("ResetPassword input not found");
    }
    fireEvent.change(emailInput, { target: { value: email } });
}

function clickSendButton() {
    const sendButton = screen.getByText("Send");
    if (sendButton == null) {
        throw new Error("ResetPassword send button not found");
    }
    sendButton.click();
}
