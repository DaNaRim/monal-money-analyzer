import { describe } from "@jest/globals";
import { act, fireEvent, screen, waitFor } from "@testing-library/react";
import { rest } from "msw";
import { setupServer } from "msw/node";
import React from "react";
import { BrowserRouter } from "react-router-dom";
import App from "../../../../app/App";
import { type ErrorResponse, ResponseErrorType } from "../../../../common/utils/formUtils";
import { getStateHandler, renderWithProviders } from "../../../../common/utils/test-utils";
import { type RegistrationDto } from "../../../../features/registration/registrationApiSlice";

describe("RegistrationPage", () => {
    const handlers = [
        rest.post("api/v1/registration", async (req, res, ctx) => {
            const { email }: RegistrationDto = await req.json();

            if (email === "Email error") {
                const error: ErrorResponse = {
                    message: "Email error",
                    type: ResponseErrorType.FIELD_VALIDATION_ERROR,
                    errorCode: "validation.user.email.invalid",
                    fieldName: "email",
                };
                return await res(ctx.status(400), ctx.json([error]));
            }
            if (email === "Passwords don't match") {
                const error: ErrorResponse = {
                    message: "Passwords don't match",
                    type: ResponseErrorType.GLOBAL_ERROR,
                    errorCode: "validation.user.password_matching",
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
    beforeEach(() => window.history.pushState({}, "Registration", "/registration"));
    afterEach(() => server.resetHandlers());
    afterAll(() => server.close());

    it("render", async () => {
        renderWithProviders(<App/>, { wrapper: BrowserRouter });

        expect(screen.getByTestId("main-loader")).toBeInTheDocument();

        await waitFor(() => {
            expect(screen.getByTestId("main-header")).toBeInTheDocument();
            expect(screen.getByTestId("main-footer")).toBeInTheDocument();
            expect(screen.getByTestId("registration-page")).toBeInTheDocument();

            expect(screen.getByText("Email")).toBeInTheDocument();
            expect(screen.getByText("Password")).toBeInTheDocument();
            expect(screen.getByText("Confirm password")).toBeInTheDocument();
            expect(screen.getByTestId("register-button")).toBeInTheDocument();
        });
    });

    // I don't know why but this test passes or fails randomly
    // It looks like button is not clicked sometimes
    it("fields not filled -> display required error messages", async () => {
        renderWithProviders(<App/>, { wrapper: BrowserRouter });

        await waitFor(() => clickRegisterButton());

        await waitFor(() => {
            expect(screen.getByText("Email is required")).toBeInTheDocument();
            expect(screen.getByText("Password is required")).toBeInTheDocument();
            expect(screen.getByText("Confirm password is required")).toBeInTheDocument();
        });
    });

    it("registration success -> display success message", async () => {
        renderWithProviders(<App/>, { wrapper: BrowserRouter });

        await waitFor(() => fillRegistrationInputs({
            email: "a@b",
            password: "123",
            matchingPassword: "123",
        }));
        clickRegisterButton();

        // should display loading message
        await waitFor(() => expect(screen.getByText("Registering...")).toBeInTheDocument());

        // should display success message
        await waitFor(() =>
            expect(screen.getByText("Registration successful."
                + " Please check your email to activate your account."
                + " If it doesn't appear within a few minutes, check your spam folder."))
                .toBeInTheDocument());
    });

    it("register field error -> display error message", async () => {
        renderWithProviders(<App/>, { wrapper: BrowserRouter });

        await waitFor(() => fillRegistrationInputs({
            email: "Email error",
            password: "123",
            matchingPassword: "123",
        }));
        clickRegisterButton();

        // should display error message
        await waitFor(() => {
            expect(screen.getByTestId("error-email"))
                .toHaveTextContent("Email must be a valid email address");
        });
    });

    it("register global error -> display error message", async () => {
        renderWithProviders(<App/>, { wrapper: BrowserRouter });

        await waitFor(() => fillRegistrationInputs({
            email: "Passwords don't match",
            password: "123",
            matchingPassword: "123",
        }));
        act((): void => clickRegisterButton());

        // should display error message
        await waitFor(() =>
            expect(screen.getByTestId("global-error-message"))
                .toHaveTextContent("Password and confirm password must match"));
    });

    it("register server error -> display error message", async () => {
        renderWithProviders(<App/>, { wrapper: BrowserRouter });

        await waitFor(() => fillRegistrationInputs({
            email: "Server error",
            password: "123",
            matchingPassword: "123",
        }));
        act((): void => clickRegisterButton());

        // should display error message
        await waitFor(() =>
            expect(screen.getByTestId("server-error-message"))
                .toHaveTextContent("Server error. Please try again later. If the problem"
                    + " persists, please contact the administrator"));
    });
});

function fillRegistrationInputs(fields: RegistrationDto) {
    const { email, password, matchingPassword } = fields;

    const emailInput = screen.getByTestId("input-email");
    const passwordInput = screen.getByTestId("input-password");
    const matchingPasswordInput = screen.getByTestId("input-matchingPassword");

    if (emailInput == null
        || passwordInput == null
        || matchingPasswordInput == null) {
        throw new Error("Registration inputs not found");
    }

    fireEvent.change(emailInput, { target: { value: email } });
    fireEvent.change(passwordInput, { target: { value: password } });
    fireEvent.change(matchingPasswordInput, { target: { value: matchingPassword } });
}

function clickRegisterButton() {
    const registerButton = screen.getByTestId("register-button");
    if (registerButton == null) {
        throw new Error("Register button not found");
    }
    registerButton.click();
}
