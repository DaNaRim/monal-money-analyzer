import {createApi, FetchArgs, fetchBaseQuery} from "@reduxjs/toolkit/query/react";
import {BaseQueryApi} from "@reduxjs/toolkit/dist/query/baseQueryTypes";
import {clearAuthState, setCredentials} from "../auth/authSlice";

const serverUrl = "/api/v1";

const baseQuery = fetchBaseQuery({
    baseUrl: serverUrl,
});

const baseQueryWithReauth = async (args: string | FetchArgs,
                                   api: BaseQueryApi,
                                   extraOptions: {},
) => {
    let result = await baseQuery(args, api, extraOptions);

    if (result.error?.status === 403) {
        const refreshResult = await baseQuery("/auth/refresh", api, extraOptions);

        if (refreshResult.error?.status === 401) {
            await baseQuery("/logout", api, extraOptions);
            api.dispatch(clearAuthState());
            //TODO: redirect to login page
        } else if (refreshResult.error?.status === 403) {
            //TODO: redirect to forbidden page
        } else if (refreshResult.error?.status.toString().startsWith("5")) {
            //TODO: redirect to server error page
        } else {
            api.dispatch(setCredentials(refreshResult.data));
            result = await baseQuery(args, api, extraOptions);
        }
    }
    return result;
};

export const apiSlice = createApi({
    baseQuery: baseQueryWithReauth,
    endpoints: () => ({}),
});
