import React, { useMemo } from "react";
import { type FieldErrors } from "react-hook-form/dist/types/errors";
import { type UseFormRegister } from "react-hook-form/dist/types/form";
import { type RegisterOptions } from "react-hook-form/dist/types/validator";
import useTranslation from "../../../../app/hooks/translation";
import styles from "./Input.module.scss";

type InputTypes = "text" | "password" | "email";

export interface InputExtProps {
    name: string;
    componentName: string;
    options?: RegisterOptions;
    register: UseFormRegister<any>;
    errors: FieldErrors;
    className?: string;
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
               }: InputProps) => {
    const t = useTranslation();

    const id = useMemo(() => `inp-${name}-${Math.random()}`, [name]);

    const label = t.getString(`${componentName}.form.fields.${name}`);

    const requiredSign = options?.required === undefined || options?.required === false
        ? ""
        : <span className={styles.required} title={t.form.required}>*</span>;

    const requiredError = t.getString(`${componentName}.form.errors.${name}.required`);

    return (
        <>
            <div className={`${styles.inputWrapper} ${className}`}>
                <input type={type}
                       id={id}
                       placeholder=" "
                       {...register(name, options)}
                       data-testid={`input-${name}`}/>
                <label htmlFor={id}>{label} {requiredSign}</label>
            </div>
            {errors?.[name]?.type === "required" &&
              <span className={styles.error}>{requiredError}</span>
            }
            {((errors?.[name]) != null) &&
              <span className={styles.error} data-testid={`error-${name}`}>
                  {errors?.[name]?.message as string}
              </span>
            }
        </>
    );
};

export default Input;
