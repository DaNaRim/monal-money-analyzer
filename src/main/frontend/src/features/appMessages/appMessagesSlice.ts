import { createSlice, type PayloadAction } from "@reduxjs/toolkit";
import { type RootState } from "../../app/store";
import { clearAuthState } from "../auth/authSlice";

/*
    App messages are messages that are shown to the user in special blocks of the page.
 */

const COOKIE_KEY_APPLICATION_MESSAGE = "serverMessage";

export enum AppMessageType {
    INFO = "INFO",
    WARNING = "WARNING",
    ERROR = "ERROR"
}

// AppMessagesCode words must be split only with underscores
export enum AppMessageCode {

    // Server messages
    // That are sent from the server. They should be here to sure what we need in development

    ACCOUNT_CONFIRMATION_SUCCESS = "account_confirmation_success", // login page

    TOKEN_WRONG_TYPE = "validation_token_wrong_type", // login page
    TOKEN_NOT_FOUND = "validation_token_not_found", // login page
    TOKEN_USED = "validation_token_used", // login page
    TOKEN_EXPIRED = "validation_token_expired", // login page

    // Specific messages because code is used to suggest resend email for user
    TOKEN_VERIFICATION_NOT_FOUND = "validation_token_verification_not_found", // login page
    TOKEN_VERIFICATION_EXPIRED = "validation_token_verification_expired", // login page
    TOKEN_VERIFICATION_USER_ENABLED = "validation_token_verification_user_enabled", // login page

    // Frontend messages
    // This app messages should be there in enum because they are created not inside components
    // where they should be shown

    PASSWORD_RESET_SUCCESS = "password_reset_success", // login page
    AUTH_EXPIRED = "auth_expired", // login page
    AUTH_NEEDED = "auth_needed" // login page
}

export interface AppMessage {
    type: AppMessageType;
    page: string;
    messageCode: AppMessageCode;
}

interface AppMessagesState {
    messages: AppMessage[];
}

const initialState: AppMessagesState = {
    messages: [],
};

export const appMessagesSlice = createSlice({
    name: "appMessages",
    initialState,
    reducers: {
        checkForServerMessages: (state) => {
            const serverMessage = document.cookie.split("; ")
                .find(row => row.startsWith(COOKIE_KEY_APPLICATION_MESSAGE))?.split("=")[1]
                .replaceAll("\\", "") // Server sends messages with escaped quotes
                .replace(/^"/, "")
                .replace(/"$/, "");

            if (serverMessage === undefined) {
                return;
            }

            const cookie: AppMessage = JSON.parse(serverMessage);
            state.messages.push({
                type: cookie.type,
                page: cookie.page,
                messageCode: cookie.messageCode,
            });
            document.cookie = `${COOKIE_KEY_APPLICATION_MESSAGE}=;`
                + " expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
        },
        addAppMessage: (state, action: PayloadAction<AppMessage>) => {
            const message = action.payload;

            state.messages.push({
                type: message.type,
                page: message.page,
                messageCode: message.messageCode,
            });
        },
        deleteAppMessage: (state, action: PayloadAction<string>) => {
            const msgCode = action.payload;

            state.messages = state.messages.filter(message => message.messageCode !== msgCode);
        },
    },
    extraReducers: builder => builder.addCase(clearAuthState, () => initialState),
});

export const {
    checkForServerMessages,
    addAppMessage,
    deleteAppMessage,
} = appMessagesSlice.actions;

export const selectAppMessages = (state: RootState) => state.appMessages.messages;

export default appMessagesSlice.reducer;
