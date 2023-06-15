import { describe, it } from "@jest/globals";
import { fireEvent, screen, waitFor } from "@testing-library/react";
import { rest } from "msw";
import { setupServer } from "msw/node";
import React from "react";
import { BrowserRouter } from "react-router-dom";
import App from "../../../../app/App";
import { type ErrorResponse, ResponseErrorType } from "../../../../app/hooks/formUtils";
import { renderWithProviders } from "../../../../common/utils/test-utils";
import { type ResetPasswordDto } from "../../../../features/registration/registrationApiSlice";

describe("ResendVerificationTokenPage", () => {
    const handlers = [
        rest.post("api/v1/resetPasswordSet", async (req, res, ctx) => {
            const { newPassword }: ResetPasswordDto = await req.json();

            if (newPassword === "Password error") {
                const error: ErrorResponse = {
                    message: "Password error",
                    type: ResponseErrorType.FIELD_VALIDATION_ERROR,
                    errorCode: "code",
                    fieldName: "newPassword",
                };
                return await res(ctx.status(400), ctx.json([error]));
            }
            if (newPassword === "Global error") {
                const error: ErrorResponse = {
                    message: "Global error",
                    type: ResponseErrorType.GLOBAL_ERROR,
                    errorCode: "code",
                    fieldName: ResponseErrorType.GLOBAL_ERROR,
                };
                return await res(ctx.status(400), ctx.json([error]));
            }
            if (newPassword === "Server error") {
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
    ];

    const server = setupServer(...handlers);

    beforeAll(() => server.listen());
    beforeEach(() => window.history.pushState({}, "Reset password set token", "/resetPasswordSet"));
    afterEach(() => server.resetHandlers());
    afterAll(() => server.close());

    it("render", async () => {
        renderWithProviders(<App/>, { wrapper: BrowserRouter });

        expect(screen.getByText("Loading...")).toBeInTheDocument();

        await waitFor(() => {
            expect(screen.getByTestId("main-header")).toBeInTheDocument();
            expect(screen.getByTestId("main-footer")).toBeInTheDocument();
            expect(screen.getByTestId("reset-password-set-page")).toBeInTheDocument();

            expect(screen.getByText("New password")).toBeInTheDocument();
            expect(screen.getByText("Confirm password")).toBeInTheDocument();
            expect(screen.getAllByText("Set new password").find(el => el.tagName === "BUTTON"))
                .toBeInTheDocument();
        });
    });

    // I don't know why but this test passes or fails randomly
    // It looks like button is not clicked sometimes
    it("fields not filled -> display required error messages", async () => {
        renderWithProviders(<App/>, { wrapper: BrowserRouter });

        await waitFor(() => clickSubmitButton());

        await waitFor(() => expect(screen.getByText("Password is required")).toBeInTheDocument());
    });

    it("reset success -> redirect to login page", async () => {
        renderWithProviders(<App/>, { wrapper: BrowserRouter });

        await waitFor(() => fillResetInputs({ newPassword: "123", matchingPassword: "123" }));

        clickSubmitButton();

        await waitFor(() => expect(screen.getByText("Processing...")).toBeInTheDocument());

        await waitFor(async () => {
            expect(screen.getByTestId("login-page")).toBeInTheDocument();
            expect(screen.getByText("Password updated successfully. You can now log in."))
                .toBeInTheDocument(); // App message
        });
    });

    it("reset field error -> display error message", async () => {
        renderWithProviders(<App/>, { wrapper: BrowserRouter });

        await waitFor(() => fillResetInputs({
            newPassword: "Password error",
            matchingPassword: "123",
        }));

        clickSubmitButton();

        await waitFor(async () => expect(screen.getByText("Password error")).toBeInTheDocument());
    });

    it("reset global error -> display error message", async () => {
        renderWithProviders(<App/>, { wrapper: BrowserRouter });

        await waitFor(() => fillResetInputs({
            newPassword: "Global error",
            matchingPassword: "123",
        }));
        clickSubmitButton();

        // should display error message
        await waitFor(() =>
            expect(screen.getByTestId("global-error"))
                .toHaveTextContent("Global error"));
    });

    it("reset server error -> display error message", async () => {
        renderWithProviders(<App/>, { wrapper: BrowserRouter });

        await waitFor(() => fillResetInputs({
            newPassword: "Server error",
            matchingPassword: "123",
        }));
        clickSubmitButton();

        // should display error message
        await waitFor(() =>
            expect(screen.getByTestId("server-error"))
                .toHaveTextContent("Server error. Please try again later. If the problem"
                    + " persists, please contact the administrator"));
    });
});

function fillResetInputs({ newPassword, matchingPassword }: ResetPasswordDto) {
    const newPasswordInput = screen.getByTestId("input-newPassword");
    const matchingPasswordInput = screen.getByTestId("input-matchingPassword");

    if (newPasswordInput == null || matchingPasswordInput == null) {
        throw new Error("ResetPasswordSet inputs not found");
    }
    fireEvent.change(newPasswordInput, { target: { value: newPassword } });
    fireEvent.change(matchingPasswordInput, { target: { value: matchingPassword } });
}

function clickSubmitButton() {
    const sendButton = screen.getAllByText("Set new password").find(el => el.tagName === "BUTTON");
    if (sendButton == null) {
        throw new Error("ResetPasswordSet button not found");
    }
    sendButton.click();
}
