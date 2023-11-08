import React from "react";
import { type FieldErrors } from "react-hook-form/dist/types/errors";
import { type RegisterOptions } from "react-hook-form/dist/types/validator";
import useTranslation from "../../../../app/hooks/translation";
import { resolveError, ResponseErrorType } from "../../../utils/formUtils";
import styles from "./ErrorField.module.scss";

interface ErrorFieldProps {
    name: string;
    componentName: string;
    errors: FieldErrors;
    // Needed to specify error message options for (min, max, minLength, maxLength, pattern)
    options?: RegisterOptions;
}

const ErrorField = ({ name, componentName, errors, options }: ErrorFieldProps) => {
    const t = useTranslation();

    const useFormError = (typeName: string) => (
        <>
            {errors?.[name]?.type === typeName &&
              <span className={styles.error}>
                      {t.formatString(t.getString(
                              `${componentName}.form.errors.${name}.${typeName}`),
                          options?.[`${typeName}`] as string,
                      )}
                  </span>
            }
        </>
    );

    return (
        <>
            {useFormError("required")}
            {useFormError("min")}
            {useFormError("max")}
            {useFormError("minLength")}
            {useFormError("maxLength")}
            {useFormError("pattern")}

            {(errors?.[name])?.type === ResponseErrorType.FIELD_VALIDATION_ERROR
                && <span className={styles.error} data-testid={`error-${name}`}>
                    {resolveError(errors?.[name]?.message as string, t)}
              </span>
            }
        </>
    );
};

export default ErrorField;
