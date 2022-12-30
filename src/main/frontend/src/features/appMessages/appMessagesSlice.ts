import {RootState} from "@app/store";
import {createSlice, PayloadAction} from "@reduxjs/toolkit";

const COOKIE_KEY_APPLICATION_MESSAGE = "serverMessage";

export enum AppMessageType {
    INFO = "INFO",
    WARNING = "WARNING",
    ERROR = "ERROR"
}

export interface ApplicationMessage {
    message: string;
    type: AppMessageType;
    page: string | null;
    messageCode: string;
}

type AppMessagesState = {
    messages: ApplicationMessage[]
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
                .find(row => row.startsWith(COOKIE_KEY_APPLICATION_MESSAGE))?.split("=")[1];

            if (serverMessage) {
                const cookie: ApplicationMessage = JSON.parse(JSON.parse(serverMessage));

                state.messages.push({
                    message: cookie.message,
                    type: cookie.type,
                    page: cookie.page,
                    messageCode: cookie.messageCode,
                });
                document.cookie = `${COOKIE_KEY_APPLICATION_MESSAGE}=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;`;
            }
        },
        addAppMessage: (state, action: PayloadAction<ApplicationMessage>) => {
            const message = action.payload;

            state.messages.push({
                message: message.message,
                type: message.type,
                page: message.page,
                messageCode: message.messageCode,
            });
        },
        deleteAppMessage: (state, action: PayloadAction<string>) => {
            const msg = action.payload;

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
