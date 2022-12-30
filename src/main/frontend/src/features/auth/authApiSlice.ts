import {apiSlice} from "../api/apiSlice";
import {AuthState} from "./authSlice";

export type Credentials = {
    username: string, //email
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
        authGetState: builder.mutation<AuthState, void>({
            query: () => ({
                url: "/auth/getState",
                method: "POST",
            }),
        }),
        authRefresh: builder.mutation<AuthState, void>({
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
    useAuthGetStateMutation,
    useAuthRefreshMutation,
} = authApiSlice;
