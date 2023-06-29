import { describe } from "@jest/globals";
import { act, fireEvent, screen, waitFor, waitForElementToBeRemoved } from "@testing-library/react";
import { rest } from "msw";
import { setupServer } from "msw/node";
import React from "react";
import { BrowserRouter } from "react-router-dom";
import App from "../../../app/App";
import { setupStore } from "../../../app/store";
import { renderWithProviders } from "../../../common/utils/test-utils";
import { type AuthResponseEntity, Role } from "../../../features/auth/authSlice";

/*
    I don`t know how to mock document.cookie, isolate it for each test. Fakes server should also
    work with this isolated cookie. So I split tests into few files. Unfortunately, there is code
    duplication.
 */

describe("apiSlice", () => {
    const handlers = [
        rest.post("api/v1/resendVerificationToken", async (req, res, ctx) => {
            const email = req.url.searchParams.get("email");

            if (req.headers.get("X-CSRF-TOKEN") !== "1234567890") {
                return await res(ctx.status(403), ctx.delay(50));
            }
            if (email === "reauth@email" && req.cookies.refresh_token !== "refreshSuccess") {
                return await res(ctx.status(401), ctx.delay(50));
            }
            return await res(ctx.status(200), ctx.delay(50));
        }),
        rest.post("api/v1/auth/refresh", async (req, res, ctx) => {
            if (req.cookies.refresh_token === "shouldFail") {
                return await res(
                    ctx.status(401),
                    ctx.cookie("refresh_token", "refreshFail"));
            }
            if (req.cookies.refresh_token === "shouldError") {
                return await res(ctx.status(500),
                    ctx.cookie("refresh_token", "refreshError"));
            }
            const refreshResponse: AuthResponseEntity = {
                username: "test",
                firstName: "test",
                lastName: "test",
                roles: [Role.ROLE_USER],
                csrfToken: "1234567890",
            };
            return await res(
                ctx.status(200),
                ctx.cookie("refresh_token", "refreshSuccess"),
                ctx.json(refreshResponse),
            );
        }),
        rest.post("api/v1/login", async (req, res, ctx) => {
            const { username } = await req.json();

            if (username === "Fail") {
                return await res(ctx.status(401));
            }
            return await res(ctx.status(200), ctx.delay(100), ctx.json({
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
        rest.post("api/v1/logout", async (req, res, ctx) => await res(ctx.status(200))),
    ];

    const server = setupServer(...handlers);

    beforeAll(() => server.listen());
    beforeEach(() => {
        // Use thigh path because one input only
        window.history.pushState({}, "Resend ver. token", "/resendVerificationToken");
    });
    afterEach(() => server.resetHandlers());
    afterAll(() => server.close());

    it("fetch with auth - authorize failed, refresh error -> navigate to error page",
        async () => {
            document.cookie = "refresh_token=shouldError;";

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
            act((): void => clickSendButton());

            await waitFor(async () => { // wait for request
                await new Promise(resolve => setTimeout(resolve, 50));
            });
            await waitForElementToBeRemoved(() => screen.getByText("Processing..."));

            await waitFor(async () => {
                expect(document.cookie.split(";") // refresh processed
                    .find((cookie) => cookie.includes("refresh_token"))?.split("=")[1])
                    .toEqual("refreshError");

                expect(store.getState().auth).toEqual({
                    username: null,
                    firstName: null,
                    lastName: null,
                    roles: [],
                    csrfToken: null,
                    isInitialized: true,
                    isForceLogin: false, // not force login
                });
                // redirected to error page
                expect(screen.getByTestId("error-page")).toBeInTheDocument();
            });
        });
});

export function fillResendInput(email: string) {
    const emailInput = screen.getByTestId("input-email");

    if (emailInput == null) {
        throw new Error("ApiSlice ResendVerificationToken input not found");
    }
    fireEvent.change(emailInput, { target: { value: email } });
}

export function clickSendButton() {
    const sendButton = screen.getByText("Send");
    if (sendButton == null) {
        throw new Error("ApiSlice ResendVerificationToken send button not found");
    }
    sendButton.click();
}
