import { TextareaAutosize } from "@mui/material";
import React, { useMemo } from "react";
import { type Control, Controller } from "react-hook-form";
import { type FieldErrors } from "react-hook-form/dist/types/errors";
import { type RegisterOptions } from "react-hook-form/dist/types/validator";
import useTranslation from "../../../../app/hooks/translation";
import ErrorField from "../ErrorField/ErrorField";
import styles from "../Input/Input.module.scss";

interface InputTextAreaProps {
    name: string;
    componentName: string;
    defaultValue?: string | number;
    minRows?: number;
    className?: string;

    options?: RegisterOptions;
    control: Control<any>;
    errors: FieldErrors;
}

const InputTextarea = ({
                           name,
                           componentName,
                           defaultValue,
                           minRows = 3,
                           className = "",
                           options,
                           control,
                           errors,
                       }: InputTextAreaProps) => {
    const t = useTranslation();

    const id = useMemo(() => `inp-${name}-${Math.random()}`, [name]);

    const label = t.getString(`${componentName}.form.fields.${name}`);

    const requiredSign = options?.required === undefined || options?.required === false
        ? ""
        : <span className={styles.required} title={t.form.required}>*</span>;

    return (
        <div>
            <div className={`${styles.inputWrapper} ${className}`}>
                <Controller
                    name={name}
                    rules={options}
                    control={control}
                    render={(controllerProps) => (
                        <TextareaAutosize
                            onChange={e => controllerProps.field.onChange(e.target.value)}
                            onBlur={controllerProps.field.onBlur}
                            id={id}
                            placeholder=" "
                            defaultValue={defaultValue}
                            minRows={minRows}
                            style={{ overflow: "auto" }}
                            data-testid={`input-${name}`}
                        />
                    )}/>
                <label htmlFor={id}>{label} {requiredSign}</label>
            </div>
            <ErrorField {...{ name, componentName, errors, options }}/>
        </div>
    );
};

export default InputTextarea;
