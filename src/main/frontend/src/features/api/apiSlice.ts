import { createSlice } from "@reduxjs/toolkit";
import { type BaseQueryApi } from "@reduxjs/toolkit/dist/query/baseQueryTypes";
import { createApi, type FetchArgs, fetchBaseQuery } from "@reduxjs/toolkit/query/react";
import { type RootState } from "../../app/store";
import { addAppMessage, AppMessageCode, AppMessageType } from "../appMessages/appMessagesSlice";
import {
    type AuthResponseEntity,
    clearAuthState,
    setCredentials,
    setForceLogin,
} from "../auth/authSlice";

const serverUrl = "/api/v1";

const baseQuery = fetchBaseQuery({
    baseUrl: serverUrl,
    prepareHeaders: (headers, { getState }) => {
        const csrfToken = (getState() as RootState).auth.csrfToken;
        if (csrfToken != null) {
            headers.set("X-CSRF-TOKEN", csrfToken);
        }
        return headers;
    },
});

const baseQueryWithReauth = async (args: string | FetchArgs,
                                   api: BaseQueryApi,
                                   extraOptions: Record<string, unknown>,
) => {
    let result = await baseQuery(args, api, extraOptions);

    if (result.meta?.response?.status === 401 && (args as FetchArgs).url !== "/login") {
        const refreshResult = await baseQuery({
            url: "/auth/refresh",
            method: "POST",
        }, api, extraOptions);

        if (refreshResult.meta?.response?.status === 200) {
            const authResult = refreshResult.data as AuthResponseEntity;

            api.dispatch(setCredentials(authResult)); // update csrf token
            result = await baseQuery(args, api, extraOptions);
        } else if (refreshResult.meta?.response?.status === 401) {
            if ((args as FetchArgs).url === "/auth/getState") { // If auth init -> no force login
                return result;
            }
            await baseQuery({ url: "/logout", method: "POST" }, api, extraOptions);
            api.dispatch(clearAuthState()); // Clear redux state
            apiSlice.util.resetApiState(); // Clear api cache
            api.dispatch(setForceLogin(true));

            api.dispatch(addAppMessage({
                type: AppMessageType.WARNING,
                messageCode: AppMessageCode.AUTH_EXPIRED,
                page: "login",
            }));
            api.dispatch(setRedirectTo("/login"));
        } else {
            api.dispatch(clearAuthState());
            api.dispatch(setRedirectTo("/error"));
        }
    }
    return result;
};

export const apiSlice = createApi({
    baseQuery: baseQueryWithReauth,
    endpoints: () => ({}),
});

/*
Used to redirect using browser router in App.tsx.
We can't redirect with hooks in baseQueryWithReauth because it is not a component.
 */
const redirectInitialState = {
    redirectTo: null,
};

export const redirectSlice = createSlice({
    name: "redirect",
    initialState: redirectInitialState,
    reducers: {
        setRedirectTo(state, action) {
            state.redirectTo = action.payload;
        },
        clearRedirect(state) {
            state.redirectTo = null;
        },
    },
    extraReducers: builder => builder.addCase(clearAuthState, () => redirectInitialState),
});

export const { setRedirectTo, clearRedirect } = redirectSlice.actions;

export const selectRedirectTo = (state: RootState) => state.redirect.redirectTo;
