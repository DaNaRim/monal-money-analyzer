import React from "react";
import { type FieldErrors } from "react-hook-form/dist/types/errors";
import { type UseFormRegister } from "react-hook-form/dist/types/form";
import useTranslation from "../../../../app/hooks/translation";
import { resolveError } from "../../../utils/formUtils";
import styles from "./ErrorGlobal.module.scss";

export interface ErrorProps {
    register: UseFormRegister<any>;
    errors: FieldErrors;
}

const ErrorGlobal = ({ register, errors }: ErrorProps) => {
    const t = useTranslation();

    return (
        <>
            <input type="hidden" {...register("globalError")} data-testid="global-error-input"/>
            {(errors.globalError != null) &&
              <span className={styles.message} data-testid="global-error-message">
                  {resolveError(errors.globalError.message as string, t)}
              </span>
            }
        </>
    );
};

export default ErrorGlobal;
