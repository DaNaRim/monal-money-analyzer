import {RootState} from "../../app/store";
import {apiSlice} from "../api/apiSlice";

export type RegistrationDto = {
    firstName: string,
    lastName: string,
    email: string,
    password: string,
    matchingPassword: string
}

export type ResetPasswordDto = {
    newPassword: string,
    matchingPassword: string
}

export const registrationApiSlice = apiSlice.injectEndpoints({
    endpoints: builder => ({
        register: builder.mutation<RootState, RegistrationDto>({
            query: (body) => ({
                url: "/registration",
                method: "POST",
                body,
            }),
        }),
        resendVerificationToken: builder.mutation<RootState, string>({
            query: (email) => ({
                url: "/resendVerificationToken",
                method: "POST",
                params: {email},
            }),
        }),
        resetPassword: builder.mutation<RootState, string>({
            query: (email) => ({
                url: "/resetPassword",
                method: "POST",
                params: {email},
            }),
        }),
        resetPasswordSet: builder.mutation<RootState, ResetPasswordDto>({
            query: (body) => ({
                url: "/resetPasswordSet",
                method: "POST",
                body,
            }),
        }),
    }),
});

export const {
    useRegisterMutation,
    useResendVerificationTokenMutation,
    useResetPasswordMutation,
    useResetPasswordSetMutation,
} = registrationApiSlice;
