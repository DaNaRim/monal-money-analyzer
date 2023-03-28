import { type UseFormSetError } from "react-hook-form/dist/types/form";
import useTranslation from "../../app/hooks/translation";

export interface FormSystemFields {
    globalError?: string;
    serverError?: string;
}

export enum ResponseErrorType {
    FIELD_VALIDATION_ERROR = "fieldValidationError",
    GLOBAL_ERROR = "globalError",
    SERVER_ERROR = "serverError"
}

export interface ErrorResponse {
    type: ResponseErrorType;
    errorCode: string;
    fieldName: string;
    message: string;
}

interface FetchUtilsReturnType {
    handleResponseError: (e: any, setError: UseFormSetError<any>) => void;
    clearFormSystemFields: (data: FormSystemFields) => void;
}

const useFetchUtils = (): FetchUtilsReturnType => {
    const t = useTranslation();

    const handleResponseError = (e: any, setError: UseFormSetError<any>): void => {
        if (e.status === 400 || e.status === 401) {
            const errorData: ErrorResponse[] = e.data;

            errorData.forEach(error => {
                setError(error.fieldName, { type: error.type, message: error.message });
            });
        } else if (e.status === "FETCH_ERROR") {
            setError("serverError", {
                type: "serverError",
                message: t.fetchErrors.fetchError,
            });
        } else if (e.status === 500) {
            setError("serverError", {
                type: "serverError",
                message: t.fetchErrors.serverError,
            });
        } else {
            setError("serverError", {
                type: "serverError",
                message: t.fetchErrors.unknownError,
            });
        }
    };

    const clearFormSystemFields = (data: FormSystemFields) => {
        delete data.globalError;
        delete data.serverError;
    };

    return { handleResponseError, clearFormSystemFields };
};

export default useFetchUtils;
