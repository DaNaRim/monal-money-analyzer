import React from "react";
import useTranslation from "../../../../app/hooks/translation";
import { resolveError } from "../../../utils/formUtils";
import { type ErrorProps } from "../ErrorGlobal/ErrorGlobal";
import styles from "./ErrorServer.module.scss";

const ErrorServer = ({ register, errors }: ErrorProps) => {
    const t = useTranslation();

    return (
        <>
            <input type="hidden" {...register("serverError")} data-testid="server-error-input"/>
            {(errors.serverError != null) &&
              <span className={styles.message} data-testid="server-error-message">
                  {resolveError(errors.serverError.message as string, t)}
              </span>
            }
        </>
    );
};

export default ErrorServer;
