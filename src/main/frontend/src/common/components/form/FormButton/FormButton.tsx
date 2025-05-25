import { ButtonHTMLAttributes } from "react";

import styles from "./FormButton.module.scss";

type FormButtonProps = ButtonHTMLAttributes<HTMLButtonElement> & {
    text: string;
}

const FormButton = ({ text, ...props }: FormButtonProps) =>
    (
        <button {...props} className={`${styles.form_button} ${props.className || ""}`}>
            {text}
        </button>
    );

export default FormButton;