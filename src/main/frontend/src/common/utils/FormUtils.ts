import {UseFormSetError} from "react-hook-form/dist/types/form";

export type FormSystemFields = {
    globalError?: string;
    serverError?: string;
}

export enum ResponseErrorType {
    FIELD_VALIDATION_ERROR = "fieldValidationError",
    GLOBAL_ERROR = "globalError",
    SERVER_ERROR = "serverError",
}

export type ErrorResponse = {
    type: ResponseErrorType,
    errorCode: string,
    fieldName: string,
    message: string
}

export const handleResponseError = (e: any, setError: UseFormSetError<any>) => {
    if (e.status === 400 || e.status === 401) {
        const errorData: ErrorResponse[] = e.data;

        errorData.forEach(error => setError(error.fieldName, {type: error.type, message: error.message}));
    } else if (e.status === "FETCH_ERROR") {
        setError("serverError", {
            type: "serverError",
            message: "Server unavailable. please try again later",
        });
    } else if (e.status === 500) {
        setError("serverError", {
            type: "serverError",
            message: "Server error. Please try again later. If the problem persists, please contact the administrator",
        });
    } else {
        setError("serverError", {
            type: "serverError",
            message: "Unknown error. Please try again later. If the problem persists, please contact the administrator",
        });
    }
};

export const clearFormSystemFields = (data: FormSystemFields) => {
    delete data.globalError;
    delete data.serverError;
};
