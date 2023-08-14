import React, { type HTMLProps, useMemo } from "react";
import { type FieldErrors } from "react-hook-form/dist/types/errors";
import { type UseFormRegister } from "react-hook-form/dist/types/form";
import { type RegisterOptions } from "react-hook-form/dist/types/validator";
import useTranslation from "../../../../app/hooks/translation";
import styles from "./Input.module.scss";

type InputTypes = "text" | "password" | "email" | "number";

export interface InputExtProps {
    name: string;
    componentName: string;
    options?: RegisterOptions;
    register: UseFormRegister<any>;
    errors: FieldErrors;
    className?: string;
    defaultValue?: string | number;
    additionalProps?: HTMLProps<HTMLInputElement>;
}

type InputProps = InputExtProps & {
    type: InputTypes;
};

const Input = ({
                   type,
                   name,
                   componentName,
                   options,
                   register,
                   errors,
                   className = "",
                   defaultValue,
                   additionalProps,
               }: InputProps) => {
    const t = useTranslation();

    const id = useMemo(() => `inp-${name}-${Math.random()}`, [name]);

    const label = t.getString(`${componentName}.form.fields.${name}`);

    const requiredSign = options?.required === undefined || options?.required === false
        ? ""
        : <span className={styles.required} title={t.form.required}>*</span>;

    const requiredError = options?.required === undefined || options?.required === false
        ? ""
        : t.getString(`${componentName}.form.errors.${name}.required`);

    return (
        <div> {/* Div is needed to always show errors under input */}
            <div className={`${styles.inputWrapper} ${className}`}>
                <input type={type}
                       id={id}
                       placeholder=" "
                       defaultValue={defaultValue}
                       {...register(name, options)}
                       {...additionalProps}
                       data-testid={`input-${name}`}/>
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

export default Input;
