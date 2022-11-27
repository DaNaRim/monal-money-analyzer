import {AuthState} from "./authSlice";
import {apiSlice} from "../api/apiSlice";

export type Credentials = {
    username: string,
    password: string,
}

export const authApiSlice = apiSlice.injectEndpoints({
    endpoints: builder => ({
        login: builder.mutation<AuthState, Credentials>({
            query: (body) => ({
                url: "/login",
                method: "POST",
                body,
            }),
        }),
        logout: builder.mutation<AuthState, void>({
            query: () => ({
                url: "/logout",
                method: "POST",
            }),
        }),
        refresh: builder.mutation<AuthState, void>({
            query: () => ({
                url: "/auth/refresh",
                method: "POST",
            }),
        }),
    }),
});

export const {
    useLoginMutation,
    useLogoutMutation,
    useRefreshMutation,
} = authApiSlice;
