import type { PreloadedState } from "@reduxjs/toolkit";
import type { RenderOptions } from "@testing-library/react";
import { render } from "@testing-library/react";
import { rest } from "msw";
import React from "react";
import { Provider } from "react-redux";
import type { AppStore, RootState } from "../../app/store";
import { setupStore } from "../../app/store";
import { type AuthResponseEntity, Role } from "../../features/auth/authSlice";

// This type interface extends the default options for render from RTL, as well
// as allows the user to specify other things such as initialState, store.
interface ExtendedRenderOptions extends Omit<RenderOptions, "queries"> {
    preloadedState?: PreloadedState<RootState>;
    store?: AppStore;
}

export function renderWithProviders(
    ui: React.ReactElement,
    {
        preloadedState = {},
        // Automatically create a store instance if no store was passed in
        store = setupStore(preloadedState),
        ...renderOptions
    }: ExtendedRenderOptions = {},
) {
    const wrapperUi = <Provider store={store}>{ui}</Provider>;

    return { store, ...render(wrapperUi, { ...renderOptions }) };
}

// Used to remove warning for unhandled request
export const getStateHandler = rest.post("/api/v1/auth/getState", async (req, res, con) => {
    const authResponse: AuthResponseEntity = {
        username: "test",
        firstName: "test",
        lastName: "test",
        roles: [Role.ROLE_USER],
        csrfToken: "test1213123",
    };
    return await res(con.status(200), con.json(authResponse));
});
