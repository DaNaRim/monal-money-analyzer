import React from "react";
import Input, { type InputExtProps } from "../Input/Input";

interface InputNumberProps extends InputExtProps {
    min?: number;
    max?: number;
    step?: number;
}

const InputNumber = (props: InputNumberProps) => {
    const additionalProps = {
        min: props.min,
        max: props.max,
        step: props.step,
    };
    return (
        <Input type="number" {...props} additionalProps={additionalProps} />
    );
};

export default InputNumber;
