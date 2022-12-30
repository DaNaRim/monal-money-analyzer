import {createSlice, PayloadAction} from "@reduxjs/toolkit";
import {RootState} from "../../app/store";

export enum Role {
    ROLE_USER = "ROLE_USER",
    ROLE_ADMIN = "ROLE_ADMIN",
}

export type AuthResponseEntity = {
    username: string; //email
    firstName: string;
    lastName: string;
    roles: Role[];
    csrfToken: string;
}

export type AuthState = {
    username: string | null; //email
    firstName: string | null;
    lastName: string | null;
    roles: Role[];
    csrfToken: string | null;

    isInitialized: boolean;
    isForceLogin: boolean,
}

const initialState: AuthState = {
    username: null, //email
    firstName: null,
    lastName: null,
    roles: [],
    csrfToken: null,

    isInitialized: false,
    isForceLogin: false,
};

const authSlice = createSlice({
    name: "auth",
    initialState,
    reducers: {
        setCredentials(state, action: PayloadAction<AuthResponseEntity | AuthState>) {
            const response = action.payload;

            state.username = response.username;
            state.firstName = response.firstName;
            state.lastName = response.lastName;
            state.roles = response.roles;
            state.csrfToken = response.csrfToken;

            state.isInitialized = true;
        },
        setInitialized(state) {
            state.isInitialized = true;
        },
        clearAuthState(state) {
            state.username = null;
            state.firstName = null;
            state.lastName = null;
            state.roles = [];
            state.csrfToken = null;
        },
        setForceLogin(state, action: PayloadAction<boolean>) {
            state.isForceLogin = action.payload;
        },
    },
});

export const {
    setCredentials,
    setInitialized,
    clearAuthState,
    setForceLogin,
} = authSlice.actions;

export const selectAuthUsername = (state: RootState) => state.auth.username;
export const selectAuthFirstname = (state: RootState) => state.auth.firstName;
export const selectAuthLastname = (state: RootState) => state.auth.lastName;
export const selectAuthIsInitialized = (state: RootState) => state.auth.isInitialized;
export const selectAuthIsForceLogin = (state: RootState) => state.auth.isForceLogin;

export default authSlice.reducer;
