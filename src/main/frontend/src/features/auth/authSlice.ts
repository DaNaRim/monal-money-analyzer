import {createSlice} from "@reduxjs/toolkit";
import {RootState} from "../../app/store";

export type Role = "ROLE_USER" | "ROLE_ADMIN";

export type AuthResponseEntity = {
    username: string;
    firstName: string;
    lastName: string;
    roles: Role[];
    csrfToken: string;
}

export type AuthState = {
    username: string | null;
    firstName: string | null;
    lastName: string | null;
    roles: Role[];
    csrfToken: string | null;
    initialized: boolean;
}

const initialState: AuthState = {
    username: null,
    firstName: null,
    lastName: null,
    roles: [],
    csrfToken: null,
    initialized: false,
};

const authSlice = createSlice({
    name: "auth",
    initialState,
    reducers: {
        setCredentials(state, action) {
            const response: AuthResponseEntity = action.payload;

            state.username = response.username;
            state.firstName = response.firstName;
            state.lastName = response.lastName;
            state.roles = response.roles;
            state.csrfToken = response.csrfToken;

            state.initialized = true;
        },
        setInitialized(state) {
            state.initialized = true;
        },
        clearAuthState(state) {
            state.username = null;
            state.firstName = null;
            state.lastName = null;
            state.roles = [];
            state.csrfToken = null;
        },
    },
});

export const {
    setCredentials,
    setInitialized,
    clearAuthState,
} = authSlice.actions;

export const selectAuthUsername = (state: RootState) => state.auth.username;
export const selectAuthFirstname = (state: RootState) => state.auth.firstName;
export const selectAuthLastname = (state: RootState) => state.auth.lastName;
export const selectAuthInitialized = (state: RootState) => state.auth.initialized;

export default authSlice.reducer;
