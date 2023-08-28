import { FormControl, InputLabel, Select } from "@mui/material";
import React, { type ReactNode, useEffect, useMemo } from "react";
import { type Control, Controller, type UseFormSetValue } from "react-hook-form";
import { type FieldErrors } from "react-hook-form/dist/types/errors";
import { type RegisterOptions } from "react-hook-form/dist/types/validator";
import useTranslation from "../../../../app/hooks/translation";
import ErrorField from "../ErrorField/ErrorField";
import errorStyles from "../Input/Input.module.scss";
import styles from "./InputSelect.module.scss";

interface InputSelectProps {
    name: string;
    componentName: string;
    defaultValue?: string | number;

    control: Control<any>;
    options?: RegisterOptions;
    errors: FieldErrors;
    setValue: UseFormSetValue<any>;

    renderValue: (id: string | number) => ReactNode;
    children: ReactNode;
}

const InputSelect = ({
                         name,
                         componentName,
                         defaultValue,
                         control,
                         options,
                         errors,
                         setValue,
                         renderValue,
                         children,
                     }: InputSelectProps) => {
    const t = useTranslation();

    const id = useMemo(() => `inp-${name}-${Math.random()}`, [name]);

    const label = t.getString(`${componentName}.form.fields.${name}`);

    const requiredSign = options?.required === undefined || options?.required === false
        ? ""
        : <span className={errorStyles.required} title={t.form.required}>*</span>;

    useEffect(() => setValue(name, defaultValue), []);

    return (
        <div>
            <Controller
                name={name}
                rules={options}
                control={control}
                render={(controllerProps) => (
                    <FormControl fullWidth={true} className={styles.form_control}>
                        <InputLabel id={id}>{label} {requiredSign}</InputLabel>
                        <Select className={styles.input_select}
                                labelId={id}
                                defaultValue={defaultValue}
                                renderValue={renderValue}
                                onChange={e => controllerProps.field.onChange(e.target.value)}
                                autoWidth={true}
                                displayEmpty={true}
                                label={label}
                        >
                            {children}
                        </Select>
                    </FormControl>
                )}
            />
            <ErrorField {...{ name, componentName, errors }}/>
        </div>
    );
};

export default InputSelect;
