import React from "react";
import {FieldErrors} from "react-hook-form/dist/types/errors";
import {UseFormRegister} from "react-hook-form/dist/types/form";
import styles from "./ErrorGlobal.module.scss";

export type ErrorProps = {
    register: UseFormRegister<any>;
    errors: FieldErrors;
}

const ErrorGlobal = ({register, errors}: ErrorProps) => (
    <>
        <input type="hidden" {...register("globalError")}/>
        {errors.globalError && <span className={styles.message}>{errors.globalError.message as string}</span>}
    </>
);

export default ErrorGlobal;
