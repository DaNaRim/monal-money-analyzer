import {apiSlice} from "../api/apiSlice";
import {RootState} from "../../app/store";

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
    }),
});

export const {
    useRegisterMutation,
} = registrationApiSlice;
