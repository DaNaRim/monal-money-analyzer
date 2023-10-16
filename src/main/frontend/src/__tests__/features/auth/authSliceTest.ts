import { describe } from "@jest/globals";
import reducer, {
    type AuthResponseEntity,
    type AuthState,
    clearAuthState,
    Role,
    setCredentials,
    setForceLogin,
    setInitialized,
} from "../../../features/auth/authSlice";

describe("authSlice", () => {
    test("init state", () => {
        expect(reducer(undefined, { type: undefined })).toEqual({
            username: null,
            roles: [],
            csrfToken: null,

            isInitialized: false,
            isForceLogin: false,
        });
    });

    test("setCredentials", () => {
        const auth: AuthResponseEntity = {
            username: "username@mail.fake",
            roles: [Role.ROLE_USER],
            csrfToken: "csrfToken",
        };
        expect(reducer(undefined, setCredentials(auth))).toEqual({
            username: "username@mail.fake",
            roles: [Role.ROLE_USER],
            csrfToken: "csrfToken",

            isInitialized: true,
            isForceLogin: false,
        });
    });

    test("setInitialized", () => {
        expect(reducer(undefined, setInitialized())).toEqual({
            username: null,
            roles: [],
            csrfToken: null,

            isInitialized: true, // set to true
            isForceLogin: false,
        });
    });

    test("clearAuthState", () => {
        const prevState: AuthState = {
            username: "username@mail.fake",
            roles: [Role.ROLE_USER],
            csrfToken: "csrfToken",

            isInitialized: true,
            isForceLogin: false,
        };
        expect(reducer(prevState, clearAuthState())).toEqual({
            username: null,
            roles: [],
            csrfToken: null,

            isInitialized: true, // should not change
            isForceLogin: false,
        });
    });

    test("setForceLogin to true", () => {
        expect(reducer(undefined, setForceLogin(true))).toEqual({
            username: null,
            roles: [],
            csrfToken: null,

            isInitialized: false,
            isForceLogin: true, // set to true
        });
    });

    test("setForceLogin to false", () => {
        expect(reducer(undefined, setForceLogin(false))).toEqual({
            username: null,
            roles: [],
            csrfToken: null,

            isInitialized: false,
            isForceLogin: false, // set to false
        });
    });
});
