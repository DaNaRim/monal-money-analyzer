import React, { HTMLProps, useMemo } from "react";
import { type FieldErrors } from "react-hook-form/dist/types/errors";
import useTranslation from "../../../../app/hooks/translation";
import ErrorField from "../ErrorField/ErrorField";
import styles from "../Input/Input.module.scss";

interface OptionInputProps {
    name: string;
    componentName: string;
    isRequired?: boolean;
    errors: FieldErrors;
    inputProps: HTMLProps<HTMLInputElement>;
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

    return (
        <div ref={ref0}>
            <div className={`${styles.inputWrapper}`}>
                <input id={id}
                       placeholder=" "
                       data-testid={`input-${name}`}
                       {...inputProps}
                />
                <label htmlFor={id}>{label} {requiredSign}</label>
            </div>
            <ErrorField {...{ name, componentName, errors }}/>
        </div>
    );
};

export default OptionInput;
