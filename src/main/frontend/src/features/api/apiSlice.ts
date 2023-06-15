import { type BaseQueryApi } from "@reduxjs/toolkit/dist/query/baseQueryTypes";
import { createApi, type FetchArgs, fetchBaseQuery } from "@reduxjs/toolkit/query/react";
import { type RootState } from "../../app/store";
import { AppMessageCode, AppMessageType, saveAppMessage } from "../appMessages/appMessagesSlice";
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
            await baseQuery({ url: "/logout", method: "POST" }, api, extraOptions);
            api.dispatch(clearAuthState());
            api.dispatch(setForceLogin(true));

            api.dispatch(saveAppMessage({
                type: AppMessageType.WARNING,
                messageCode: AppMessageCode.AUTH_EXPIRED,
                page: "login",
            }));

            window.history.replaceState(null, "", "/login");
        } else {
            api.dispatch(clearAuthState());
            window.history.replaceState(null, "", "/error");
        }
    }
    return result;
};

export const apiSlice = createApi({
    baseQuery: baseQueryWithReauth,
    endpoints: () => ({}),
});
