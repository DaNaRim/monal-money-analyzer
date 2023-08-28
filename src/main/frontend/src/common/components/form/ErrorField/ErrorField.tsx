import React from "react";
import { type FieldErrors } from "react-hook-form/dist/types/errors";
import useTranslation from "../../../../app/hooks/translation";
import styles from "./ErrorField.module.scss";

interface ErrorFieldProps {
    name: string;
    componentName: string;
    errors: FieldErrors;
}

const ErrorField = ({ name, componentName, errors }: ErrorFieldProps) => {
    const t = useTranslation();

    return (
        <>
            {errors?.[name]?.type === "required" &&
              <span className={styles.error}>
                  {t.getString(`${componentName}.form.errors.${name}.required`)}
              </span>
            }
            {((errors?.[name]) != null && errors?.[name]?.type !== "required") &&
              <span className={styles.error} data-testid={`error-${name}`}>
                  {errors?.[name]?.message as string}
              </span>
            }
        </>
    );
};

export default ErrorField;
