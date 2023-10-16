import { createSlice, type PayloadAction } from "@reduxjs/toolkit";
import { type RootState } from "../../app/store";

export enum Role {
    ROLE_USER = "ROLE_USER",
    ROLE_ADMIN = "ROLE_ADMIN"
}

export interface AuthResponseEntity {
    username: string; // email
    roles: Role[];
    csrfToken: string;
}

export interface AuthState {
    username: string | null; // email
    roles: Role[];
    csrfToken: string | null;

    isInitialized: boolean;
    isForceLogin: boolean;
}

const initialState: AuthState = {
    username: null, // email
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
            state.roles = response.roles;
            state.csrfToken = response.csrfToken;

            state.isInitialized = true;
        },
        setInitialized(state) {
            state.isInitialized = true;
        },
        clearAuthState(state) {
            state.username = null;
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
export const selectAuthRoles = (state: RootState) => state.auth.roles;
export const selectAuthIsInitialized = (state: RootState) => state.auth.isInitialized;
export const selectAuthIsForceLogin = (state: RootState) => state.auth.isForceLogin;

export default authSlice.reducer;
