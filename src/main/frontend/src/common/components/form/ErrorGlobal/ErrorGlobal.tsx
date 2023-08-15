import React from "react";
import { type FieldErrors } from "react-hook-form/dist/types/errors";
import { type UseFormRegister } from "react-hook-form/dist/types/form";
import styles from "./ErrorGlobal.module.scss";

export interface ErrorProps {
    register: UseFormRegister<any>;
    errors: FieldErrors;
}

const ErrorGlobal = ({ register, errors }: ErrorProps) => (
    <>
        <input type="hidden" {...register("globalError")} data-testid="global-error-input"/>
        {(errors.globalError != null) &&
          <span className={styles.message} data-testid="global-error-message">
              {errors.globalError.message as string}
          </span>
        }
    </>
);

export default ErrorGlobal;
