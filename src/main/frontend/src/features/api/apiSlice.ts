import {BaseQueryApi} from "@reduxjs/toolkit/dist/query/baseQueryTypes";
import {createApi, FetchArgs, fetchBaseQuery} from "@reduxjs/toolkit/query/react";
import {RootState} from "../../app/store";
import {AuthResponseEntity, clearAuthState, setCredentials, setForceLogin} from "../auth/authSlice";

const serverUrl = "/api/v1";

const baseQuery = fetchBaseQuery({
    baseUrl: serverUrl,
    prepareHeaders: (headers, {getState}) => {
        const csrfToken = (getState() as RootState).auth.csrfToken;
        if (csrfToken) {
            headers.set("X-CSRF-TOKEN", csrfToken);
        }
        return headers;
    },
});

const baseQueryWithReauth = async (args: string | FetchArgs,
                                   api: BaseQueryApi,
                                   extraOptions: {},
) => {
    let result = await baseQuery(args, api, extraOptions);

    if (result.error?.status === 403) {
        const refreshResult = await baseQuery("/auth/refresh", api, extraOptions);

        if (refreshResult.meta?.response?.status === 200) {
            const authResult = refreshResult.data as AuthResponseEntity;

            api.dispatch(setCredentials(authResult));
            result = await baseQuery(args, api, extraOptions);

        } else if (refreshResult.error?.status === 401) {
            await baseQuery("/logout", api, extraOptions);

            api.dispatch(clearAuthState());
            api.dispatch(setForceLogin(true));

            window.location.replace("/login");
        } else if (refreshResult.error?.status === 403) {
            window.location.replace("/forbidden");
        } else {
            window.location.replace("/error");
        }
    }
    return result;
};

export const apiSlice = createApi({
    baseQuery: baseQueryWithReauth,
    endpoints: () => ({}),
});
