import {createSlice, PayloadAction} from "@reduxjs/toolkit";
import {RootState} from "../../app/store";

const COOKIE_KEY_APPLICATION_MESSAGE = "serverMessage";

export type AppMessageType = "INFO" | "WARNING" | "ERROR";

export interface ApplicationMessage {
    message: string;
    type: AppMessageType;
    page: string | null;
    expectClientActionCode: string | null;
}

type AppMessageState = {
    messages: ApplicationMessage[]
}

const initialState: AppMessageState = {
    messages: [],
};

export const appMessagesSlice = createSlice({
    name: "appMessages",
    initialState,
    reducers: {
        checkForServerMessages: (state) => {
            const serverMessage = document.cookie.split("; ")
                .find(row => row.startsWith(COOKIE_KEY_APPLICATION_MESSAGE))?.split("=")[1];

            if (serverMessage) {
                const cookie: ApplicationMessage = JSON.parse(JSON.parse(serverMessage));

                state.messages.push({
                    message: cookie.message,
                    type: cookie.type,
                    page: cookie.page,
                    expectClientActionCode: cookie.expectClientActionCode,
                });
                document.cookie = COOKIE_KEY_APPLICATION_MESSAGE + "=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
            }
        },
        addAppMessage: (state, action: PayloadAction<ApplicationMessage>) => {
            const message = action.payload;

            state.messages.push({
                message: message.message,
                type: message.type,
                page: message.page,
                expectClientActionCode: message.expectClientActionCode,
            });
        },
        deleteAppMessage: (state, action) => {
            const {message: msg} = action.payload;

            state.messages = state.messages.filter(message => message.message !== msg);
        },
    },
});

export const {
    checkForServerMessages,
    addAppMessage,
    deleteAppMessage,
} = appMessagesSlice.actions;

export const selectAppMessages = (state: RootState) => state.appMessages.messages;

export default appMessagesSlice.reducer;
