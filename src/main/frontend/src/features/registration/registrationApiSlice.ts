import {RootState} from "../../app/store";
import {apiSlice} from "../api/apiSlice";

export type RegistrationDto = {
    firstName: string,
    lastName: string,
    email: string,
    password: string,
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
    }),
});

export const {
    useRegisterMutation,
    useResendVerificationTokenMutation,
} = registrationApiSlice;
