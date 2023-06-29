import { apiSlice } from "../api/apiSlice";
import { type AuthState } from "./authSlice";

export interface Credentials {
    username: string; // email
    password: string;
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
    }),
});

export const {
    useLoginMutation,
    useLogoutMutation,
    useAuthGetStateMutation,
} = authApiSlice;
