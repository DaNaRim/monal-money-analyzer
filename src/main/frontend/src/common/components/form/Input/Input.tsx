import {useMemo} from "react";
import {FieldErrors} from "react-hook-form/dist/types/errors";
import {UseFormRegister} from "react-hook-form/dist/types/form";
import {RegisterOptions} from "react-hook-form/dist/types/validator";
import useTranslation from "../../../../app/hooks/translation";
import styles from "./Input.module.scss";

type InputTypes = "text" | "password" | "email";

export type InputExtProps = {
    name: string;
    componentName: string;
    options?: RegisterOptions;
    register: UseFormRegister<any>;
    errors: FieldErrors;
    className?: string;
}

type InputProps = InputExtProps & {
    type: InputTypes;
}

const Input = ({type, name, componentName, options, register, errors, className}: InputProps) => {
    const t = useTranslation();

    const id = useMemo(() => `inp-${name}-${Math.random()}`, [name]);

    const extraClass = className ? `${className}` : "";

    const label = t.getString(`${componentName}.form.fields.${name}`);

    const requiredSign = options?.required ? <span className={styles.required} title={t.form.required}>*</span> : "";

    const requiredError = t.getString(`${componentName}.form.errors.${name}.required`);

    console.log(errors);
    console.log();

    return (
        <>
            <div className={`${styles.inputWrapper} ${extraClass}`}>
                <input type={type} id={id} placeholder=" " {...register(name, options)}/>
                <label htmlFor={id}>{label} {requiredSign}</label>
            </div>
            {errors?.[name]?.type === "required" && <span className={styles.error}>{requiredError}</span>}
            {errors?.[name] && <span className={styles.error}>{errors?.[name]?.message as string}</span>}
        </>
    );
};

export default Input;
