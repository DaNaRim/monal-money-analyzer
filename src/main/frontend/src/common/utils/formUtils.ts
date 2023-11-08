import { type UseFormSetError } from "react-hook-form/dist/types/form";
import { type LocalizedStrings } from "react-localization";
import { type Localization } from "../../i18n";

/**
 * Form system fields are fields that are not related to form data. It is used by react-hook-form
 * library to store form errors.
 */
export interface FormSystemFields {
    globalError?: string;
    serverError?: string;
}

export enum ResponseErrorType {
    FIELD_VALIDATION_ERROR = "fieldValidationError",
    GLOBAL_ERROR = "globalError",
    SERVER_ERROR = "serverError"
}

/**
 * Response error object that is returned by the server.
 */
export interface ErrorResponse {
    type: ResponseErrorType;
    errorCode: string;
    errorArgs?: string[];
    fieldName: string;
    message: string;
}

/**
 * Handles response errors and sets error template to message in react-hook-form setError function.
 *
 * Message template: "{errorCode}:[arg1,arg2,...]" or "fetchError" or "serverError"
 * or "unknownError". This is used to localize error messages. Use it with {@link resolveError}
 * function.
 *
 * There are three types of errors:
 * - field validation error: error in a form field
 * - global error: error in a form. For example, when password and password confirmation do not
 * match
 * - server error: error from the server or network error
 *
 * Example of usage:
 * ```typescript
 *     login(data).unwrap()
 *         .then(data => doSomething(data))
 *         .catch(e => handleResponseError(e, setError)); // <- here
 * ```
 *
 * @param e - error response
 * @param setError - react-hook-form setError function
 */
export const handleResponseError = (e: any, setError: UseFormSetError<any>): void => {
    if (e.status === 400 || e.status === 401) {
        const errorData: ErrorResponse[] = e.data ?? [];

        errorData.forEach(error => {
            const preparedArgs = error.errorArgs ?? [];
            const preparedMessage = `{${error.errorCode}}:[${preparedArgs.toString()}]`;

            setError(error.fieldName, { type: error.type, message: preparedMessage });
        });
    } else if (e.status === "FETCH_ERROR") {
        setError(ResponseErrorType.SERVER_ERROR, {
            type: ResponseErrorType.SERVER_ERROR,
            message: "fetchError",
        });
    } else if (e.status === 500) {
        setError(ResponseErrorType.SERVER_ERROR, {
            type: ResponseErrorType.SERVER_ERROR,
            message: "serverError",
        });
    } else {
        setError(ResponseErrorType.SERVER_ERROR, {
            type: ResponseErrorType.SERVER_ERROR,
            message: "unknownError",
        });
    }
};

/**
 * Resolves an error message from error code and arguments to localized string.
 * If the error code is not found in a localization object, then a default error message is set.
 *
 * @param codeAndArgs - error code and arguments in format "{code}:[arg1,arg2,...]" or
 * "fetchError" or "serverError" or "unknownError"
 * @param t - localization object from {@link useTranslation} hook
 */
export const resolveError = (codeAndArgs: string, t: LocalizedStrings<Localization>): string => {
    if (codeAndArgs === "fetchError"
        || codeAndArgs === "serverError"
        || codeAndArgs === "unknownError") {
        return t.fetchErrors[codeAndArgs];
    }
    const code = codeAndArgs.substring(1, codeAndArgs.indexOf(":") - 1);
    const argsStr = codeAndArgs.split(":")[1];
    const args = argsStr.substring(1, argsStr.length - 1).split(",");

    if (t.getString(code, t.getLanguage(), true) === null) {
        return t.formatString(t.validation.unknown, code) as string;
    }
    return t.formatString(t.getString(code, t.getLanguage(), true), ...args) as string;
};

/**
 * Clears form system fields. Used do delete fields that are not necessary to send to the server.
 *
 * @param data - form data
 */
export const clearFormSystemFields = (data: FormSystemFields) => {
    delete data.globalError;
    delete data.serverError;
};
