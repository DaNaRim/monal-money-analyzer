import React from "react";
import { type ErrorProps } from "../ErrorGlobal/ErrorGlobal";
import styles from "./ErrorServer.module.scss";

const ErrorServer = ({ register, errors }: ErrorProps) => (
    <>
        <input type="hidden" {...register("serverError")}/>
        {(errors.serverError != null) &&
          <span className={styles.message} data-testid="server-error">
              {errors.serverError.message as string}
          </span>
        }
    </>
);

export default ErrorServer;
