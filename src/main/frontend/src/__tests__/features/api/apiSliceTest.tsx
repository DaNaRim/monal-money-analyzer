import { describe } from "@jest/globals";
import { act, fireEvent, screen, waitFor, waitForElementToBeRemoved } from "@testing-library/react";
import { rest } from "msw";
import { setupServer } from "msw/node";
import { BrowserRouter } from "react-router-dom";
import App from "../../../app/App";
import { setupStore } from "../../../app/store";
import { renderWithProviders } from "../../../common/utils/test-utils";
import { Role } from "../../../features/auth/authSlice";

/*
    I don`t know how to mock document.cookie, isolate it for each test. Fakes erver should also work
    with this isolated cookie. So I comment some tests. Each test should be run separately.
 */

describe("apiSlice", () => {
    const handlers = [
        rest.post("api/v1/resendVerificationToken", (req, res, ctx) => {
            const email = req.url.searchParams.get("email");

            if (req.headers.get("X-CSRF-TOKEN") !== "1234567890") {
                return res(ctx.status(403));
            }
            if (email === "reauth@email" && req.cookies.refresh_token !== "refreshSuccess") {
                return res(ctx.status(401));
            }
            return res(ctx.status(200));
        }),
        rest.post("api/v1/auth/refresh", (req, res, ctx) => {
            if (req.cookies.refresh_token === "shouldFail") {
                return res(
                    ctx.status(401),
                    ctx.cookie("refresh_token", "refreshFail"));
            }
            if (req.cookies.refresh_token === "shouldError") {
                return res(ctx.status(500),
                    ctx.cookie("refresh_token", "refreshError"));
            }
            return res(
                ctx.status(200),
                ctx.cookie("refresh_token", "refreshSuccess"),
            );
        }),
        rest.post("api/v1/login", (req, res, ctx) => {
            return req.json().then(data => {
                if (data.username === "Fail") {
                    return res(ctx.status(401));
                }
                return res(ctx.status(200), ctx.json({
                    username: "John",
                    firstName: "John",
                    lastName: "Smith",
                    roles: ["ROLE_USER"],
                    csrfToken: "1234567890",
                }));
            });
        }),
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
        rest.post("api/v1/logout", (req, res, ctx) => {
            return res(ctx.status(200));
        }),
    ];

    const server = setupServer(...handlers);

    beforeAll(() => server.listen());
    beforeEach(() => {
        // Use thigh path because one input only
        window.history.pushState({}, "Resend ver. token", "/resendVerificationToken");
        document.cookie = "refresh_token=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
    });
    afterEach(() => {
        server.resetHandlers();
    });
    afterAll(() => server.close());

    it("fetch with auth -> add csrf token to request header", async () => {
        const store = setupStore({
            auth: {
                username: "test",
                firstName: "test",
                lastName: "test",
                roles: [Role.ROLE_USER],
                csrfToken: "1234567890",
                isInitialized: true,
                isForceLogin: false,
            },
        });
        renderWithProviders(<App/>, { wrapper: BrowserRouter, store });

        await waitFor(() => fillResendInput("test@email"));
        clickSendButton();

        await waitFor(async () => {
            expect(screen.getByText("Verification email sent."
                + " Please check your email to activate your account."
                + " If it doesn't appear within a few minutes, check your spam folder."))
                .toBeInTheDocument();
        });
    });

    // it("fetch with auth - authorize failed -> reauth", async () => {
    //     document.cookie = "refresh_token=refresh;";
    //
    //     const store = setupStore({
    //         auth: {
    //             username: "test",
    //             firstName: "test",
    //             lastName: "test",
    //             roles: [Role.ROLE_USER],
    //             csrfToken: "1234567890",
    //             isInitialized: true,
    //             isForceLogin: false,
    //         },
    //     });
    //     renderWithProviders(<App/>, { wrapper: BrowserRouter, store });
    //
    //     await waitFor(async () => fillResendInput("reauth@email"));
    //     await act(() => clickSendButton());
    //
    //     await waitForElementToBeRemoved(() => screen.getByText("Processing..."));
    //
    //     await waitFor(async () => {
    //         expect(screen.getByText("Verification email sent."
    //             + " Please check your email to activate your account."
    //             + " If it doesn't appear within a few minutes, check your spam folder."))
    //             .toBeInTheDocument();
    //         expect(document.cookie.split(";") // refresh processed
    //             .find((cookie) => cookie.includes("refresh_token"))?.split("=")[1])
    //             .toEqual("refreshSuccess");
    //         expect(store.getState().auth).toEqual({ // auth state not changed
    //             username: "test",
    //             firstName: "test",
    //             lastName: "test",
    //             roles: [Role.ROLE_USER],
    //             csrfToken: "1234567890",
    //             isInitialized: true,
    //             isForceLogin: false,
    //         });
    //     });
    // });

    it("fetch with auth - authorize failed, refresh failed -> logout", async () => {
        document.cookie = "refresh_token=shouldFail;";

        const store = setupStore({
            auth: {
                username: "test",
                firstName: "test",
                lastName: "test",
                roles: [Role.ROLE_USER],
                csrfToken: "1234567890",
                isInitialized: true,
                isForceLogin: false,
            },
        });
        renderWithProviders(<App/>, { wrapper: BrowserRouter, store });

        await waitFor(async () => fillResendInput("reauth@email"));
        await act(() => clickSendButton());

        await waitForElementToBeRemoved(() => screen.getByText("Processing..."));

        await waitFor(async () => {
            expect(document.cookie.split(";") // refresh processed
                .find((cookie) => cookie.includes("refresh_token"))?.split("=")[1])
                .toEqual("refreshFail");

            expect(store.getState().auth).toEqual({
                username: null,
                firstName: null,
                lastName: null,
                roles: [],
                csrfToken: null,
                isInitialized: true,
                isForceLogin: true,
            });
            //Can`t handle redirect because of not react redirect (window.location.href)
            // expect(screen.getByTestId("login-page")).toBeInTheDocument(); //redirected to login page
        });

        // await waitFor(async () => {
        //     expect(store.getState().appMessages).toEqual({
        //         messages: [{
        //             type: AppMessageType.WARNING,
        //             messageCode: AppMessageCode.AUTH_EXPIRED,
        //             page: "login",
        //         }]
        //     });
        //     // Display message
        //     expect(screen.getByText("Session expired. Please log in again.")).toBeInTheDocument();
        // });
    });


    // it("fetch with auth - authorize failed, refresh error -> navigate to error page", async () => {
    //     document.cookie = "refresh_token=shouldError;";
    //
    //     const store = setupStore({
    //         auth: {
    //             username: "test",
    //             firstName: "test",
    //             lastName: "test",
    //             roles: [Role.ROLE_USER],
    //             csrfToken: "1234567890",
    //             isInitialized: true,
    //             isForceLogin: false,
    //         },
    //     });
    //     renderWithProviders(<App/>, { wrapper: BrowserRouter, store });
    //
    //     await waitFor(async () => fillResendInput("reauth@email"));
    //     await act(() => clickSendButton());
    //
    //     await waitForElementToBeRemoved(() => screen.getByText("Processing..."));
    //
    //     await waitFor(async () => {
    //         expect(document.cookie.split(";") // refresh processed
    //             .find((cookie) => cookie.includes("refresh_token"))?.split("=")[1])
    //             .toEqual("refreshError");
    //
    //         expect(store.getState().auth).toEqual({
    //             username: null,
    //             firstName: null,
    //             lastName: null,
    //             roles: [],
    //             csrfToken: null,
    //             isInitialized: true,
    //             isForceLogin: false, // not force login
    //         });
    //         // Can`t handle redirect because of not react redirect (window.location.href)
    //         // expect(screen.getByTestId("error-page")).toBeInTheDocument(); //redirected to error page
    //     });
    // });

    it("fetch with auth - authorize failed, login req -> no refresh", async () => {
        window.history.pushState({}, "Resend ver. token", "/login");

        renderWithProviders(<App/>, { wrapper: BrowserRouter });

        await waitFor(() => fillLoginInputs("Fail", "123"));
        await act(() => clickLoginButton());
        //
        await waitForElementToBeRemoved(() => screen.getByText("Logging in..."));

        expect(document.cookie).toEqual(""); // no refresh processed
    });
});

export function fillResendInput(email: string) {
    const emailInput = screen.getByTestId("input-email") as HTMLInputElement;

    if (!emailInput) {
        throw new Error("ApiSlice ResendVerificationToken input not found");
    }
    fireEvent.change(emailInput, { target: { value: email } });
}

export function clickSendButton() {
    const sendButton = screen.getByText("Send");
    if (!sendButton) {
        throw new Error("ApiSlice ResendVerificationToken send button not found");
    }
    sendButton.click();
}

function fillLoginInputs(username: string, password: string) {
    const emailInput = screen.getByTestId("input-username");
    const passwordInput = screen.getByTestId("input-password");

    if (!emailInput || !passwordInput) {
        throw new Error("ApiSlice Login inputs not found");
    }
    fireEvent.change(emailInput, { target: { value: username } });
    fireEvent.change(passwordInput, { target: { value: password } });
}

function clickLoginButton() {
    const loginButton = screen.getAllByText("Login")
        .find(el => el.tagName === "BUTTON") as HTMLButtonElement;

    if (!loginButton) {
        throw new Error("ApiSlice Login button not found");
    }
    loginButton.click();
}
