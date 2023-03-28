import { createSlice, type PayloadAction } from "@reduxjs/toolkit";
import { type RootState } from "../../app/store";

const COOKIE_KEY_APPLICATION_MESSAGE = "serverMessage";

export enum AppMessageType {
    INFO = "INFO",
    WARNING = "WARNING",
    ERROR = "ERROR"
}

export enum AppMessageCode {

    // server messages

    UNRESOLVED_CODE = "unresolved",

    REGISTRATION_CONFIRMATION_SUCCESS = "registration.confirmation.success",

    TOKEN_WRONG_TYPE = "validation.token.wrong-type",
    TOKEN_NOT_FOUND = "validation.token.not-found",
    TOKEN_USED = "validation.token.used",
    TOKEN_EXPIRED = "validation.token.expired",

    TOKEN_VERIFICATION_NOT_FOUND = "validation.token.verification.not-found",
    TOKEN_VERIFICATION_EXPIRED = "validation.token.verification.expired",
    TOKEN_VERIFICATION_USER_ENABLED = "validation.token.verification.user-enabled",

    // frontend messages

    PASSWORD_RESET_SUCCESS = "password-reset.success",
    AUTH_EXPIRED = "auth.expired"
}

export interface AppMessage {
    type: AppMessageType;
    page: string | null;
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
        saveAppMessage: (state, action: PayloadAction<AppMessage>) => {
            const message = action.payload;

            state.messages.push({
                type: message.type,
                page: message.page,
                messageCode: message.messageCode,
            });

            const expires = new Date(Date.now() + 5 * 60 * 1000).toUTCString();

            document.cookie = `${COOKIE_KEY_APPLICATION_MESSAGE}=${JSON.stringify(message)};`
                + ` path=/; expires=${expires}`; // 5 minutes is enough
        },
        deleteAppMessage: (state, action: PayloadAction<string>) => {
            const msgCode = action.payload;

            state.messages = state.messages.filter(message => message.messageCode !== msgCode);
        },
    },
});

export const {
    checkForServerMessages,
    addAppMessage,
    saveAppMessage,
    deleteAppMessage,
} = appMessagesSlice.actions;

export const selectAppMessages = (state: RootState) => state.appMessages.messages;

export default appMessagesSlice.reducer;
