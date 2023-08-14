import React, { useMemo } from "react";
import { type FieldErrors } from "react-hook-form/dist/types/errors";
import useTranslation from "../../../../app/hooks/translation";
import styles from "../Input/Input.module.scss";

interface OptionInputProps {
    name: string;
    componentName: string;
    isRequired?: boolean;
    errors: FieldErrors;
    inputProps: any;
    ref0?: React.Ref<HTMLInputElement>;
}

// Input used for a Mui Select component
const OptionInput = (props: OptionInputProps) => {
    const {
        name,
        componentName,
        isRequired = false,
        errors,
        ref0,
        inputProps,
    } = props;

    const t = useTranslation();

    const id = useMemo(() => `inp-${name}-${Math.random()}`, [name]);

    const label = t.getString(`${componentName}.form.fields.${name}`);

    const requiredSign = isRequired
        ? <span className={styles.required} title={t.form.required}>*</span>
        : "";

    const requiredError = isRequired
        ? t.getString(`${componentName}.form.errors.${name}.required`)
        : "";

    return (
        <div ref={ref0}> {/* Div is needed to always show errors under input */}
            <div className={`${styles.inputWrapper}`}>
                <input id={id}
                       placeholder=" "
                       data-testid={`input-${name}`}
                       {...inputProps}
                />
                <label htmlFor={id}>{label} {requiredSign}</label>
            </div>
            {errors?.[name]?.type === "required" &&
              <span className={styles.error}>{requiredError}</span>
            }
            {/* Don't show an error block if it is null or a required error */}
            {((errors?.[name]) != null && errors?.[name]?.type !== "required") &&
              <span className={styles.error} data-testid={`error-${name}`}>
                  {errors?.[name]?.message as string}
              </span>
            }
        </div>
    );
};

export default OptionInput;
