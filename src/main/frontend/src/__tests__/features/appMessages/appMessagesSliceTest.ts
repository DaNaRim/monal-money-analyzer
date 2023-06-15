import { describe } from "@jest/globals";
import reducer, {
    addAppMessage,
    type AppMessage,
    AppMessageCode,
    AppMessageType,
    checkForServerMessages,
    deleteAppMessage,
    saveAppMessage,
} from "../../../features/appMessages/appMessagesSlice";

describe("appMessagesSlice", () => {
    afterEach(() => {
        document.cookie = "serverMessage=; expires=Thu, 01 Jan 1970 00:00:00 UTC; path=/;";
    });

    test("init state", () => {
        expect(reducer(undefined, { type: undefined })).toEqual({
            messages: [],
        });
    });

    test("checkForServerMessages", () => {
        const message: AppMessage = {
            type: AppMessageType.INFO,
            messageCode: AppMessageCode.REGISTRATION_CONFIRMATION_SUCCESS,
            page: "login",
        };
        document.cookie = `serverMessage=${JSON.stringify(message)};`;

        expect(reducer(undefined, checkForServerMessages)).toEqual({
            messages: [message],
        });
        expect(document.cookie).toEqual(""); // cookie should be deleted
    });

    test("checkForServerMessages - empty message -> no change", () => {
        expect(reducer(undefined, checkForServerMessages)).toEqual({
            messages: [],
        });
    });

    test("checkForServerMessages - escaped characters -> parsed correctly", () => {
        const message: AppMessage = {
            type: AppMessageType.WARNING,
            messageCode: AppMessageCode.TOKEN_EXPIRED,
            page: "login",
        };
        const escapedMessage = "\"".concat(JSON.stringify(message)
            .replace(/"/g, "\\\"")
            .concat("\""));

        document.cookie = `serverMessage=${escapedMessage};`;

        expect(reducer(undefined, checkForServerMessages)).toEqual({
            messages: [message],
        });
        expect(document.cookie).toEqual(""); // cookie should be deleted
    });

    test("addAppMessage - no messages -> success add", () => {
        const message: AppMessage = {
            type: AppMessageType.INFO,
            messageCode: AppMessageCode.PASSWORD_RESET_SUCCESS,
            page: "login",
        };
        expect(reducer(undefined, addAppMessage(message))).toEqual({
            messages: [message],
        });
    });

    test("addAppMessage - existing messages -> success add", () => {
        const prevState = {
            messages: [
                {
                    type: AppMessageType.INFO,
                    messageCode: AppMessageCode.PASSWORD_RESET_SUCCESS,
                    page: "login",
                },
            ],
        };
        const newMessage: AppMessage = {
            type: AppMessageType.INFO,
            messageCode: AppMessageCode.REGISTRATION_CONFIRMATION_SUCCESS,
            page: "login",
        };
        expect(reducer(prevState, addAppMessage(newMessage))).toEqual({
            messages: [prevState.messages[0], newMessage],
        });
    });

    test("saveAppMessage -> save to cookie", () => {
        const message: AppMessage = {
            type: AppMessageType.INFO,
            messageCode: AppMessageCode.REGISTRATION_CONFIRMATION_SUCCESS,
            page: "login",
        };
        expect(reducer(undefined, saveAppMessage(message))).toEqual({
            messages: [message],
        });
        expect(document.cookie === "").toBeFalsy(); // cookie should be set
    });

    test("deleteAppMessage -> delete from state", () => {
        const prevState = {
            messages: [
                {
                    type: AppMessageType.INFO,
                    messageCode: AppMessageCode.PASSWORD_RESET_SUCCESS,
                    page: "login",
                },
            ],
        };
        expect(reducer(prevState, deleteAppMessage(prevState.messages[0].messageCode))).toEqual({
            messages: [],
        });
    });
});
