import React, { type HTMLProps, useMemo } from "react";
import { type FieldErrors } from "react-hook-form/dist/types/errors";
import { type UseFormRegister } from "react-hook-form/dist/types/form";
import { type RegisterOptions } from "react-hook-form/dist/types/validator";
import useTranslation from "../../../../app/hooks/translation";
import ErrorField from "../ErrorField/ErrorField";
import styles from "./Input.module.scss";

// Supported input types for this component
type InputTypes = "text" | "password" | "email" | "number" | "date" | "datetime-local";

export interface InputExtProps {
    name: string; // Name of the field for react-hook-form and i18n
    componentName: string; // Name of the component for i18n parent key
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

    return (
        <div>
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
            <ErrorField {...{ name, componentName, errors }}/>
        </div>
    );
};

export default Input;
