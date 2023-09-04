import React, { useMemo } from "react";
import { type FieldErrors } from "react-hook-form/dist/types/errors";
import { type UseFormRegister } from "react-hook-form/dist/types/form";
import useTranslation from "../../../../app/hooks/translation";
import ErrorField from "../ErrorField/ErrorField";
import styles from "../Input/Input.module.scss";
import buttonStyles from "./InputButton.module.scss";

interface InputButtonProps {
    name: string;
    componentName: string;
    onClick: () => void;
    // Value managed by React Hook Form and external component. Displayed in the button.
    displayValue: string | undefined;
    label: string;
    isRequired?: boolean;

    register: UseFormRegister<any>;
    errors: FieldErrors;
}

const InputButton = ({
                         name,
                         componentName,
                         register,
                         errors,
                         displayValue,
                         label,
                         isRequired = false,
                         onClick,
                     }: InputButtonProps) => {
    const t = useTranslation();

    const id = useMemo(() => `inp-${name}-${Math.random()}`, [name]);

    const requiredSign = isRequired
        ? <span className={styles.required} title={t.form.required}>*</span>
        : "";

    return (
        <div>
            <div className={`${styles.inputWrapper} ${buttonStyles.buttonWrapper}`}>
                <input type="button"
                       id={id}
                       value={displayValue ?? ""}
                       onClick={onClick}/>
                <label htmlFor={id}>{label} {requiredSign}</label>
                <input type="hidden" {...register(name, { required: isRequired })}/>
            </div>
            <ErrorField {...{ name, componentName, errors }}/>
        </div>
    );
};

export default InputButton;
