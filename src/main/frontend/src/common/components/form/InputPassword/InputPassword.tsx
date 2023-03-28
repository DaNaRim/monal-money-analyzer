import { faEye } from "@fortawesome/free-regular-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import React, { useState } from "react";
import useTranslation from "../../../../app/hooks/translation";
import Input, { type InputExtProps } from "../Input/Input";
import styles from "./InputPassword.module.scss";

const InputPassword = (props: InputExtProps) => {
    const t = useTranslation();

    const [isPasswordVisible, setIsPasswordVisible] = useState(false);

    const handleKeyDown = (event: React.KeyboardEvent<HTMLButtonElement>, state: boolean) => {
        if (event.key === " ") {
            setIsPasswordVisible(state);
        }
    };

    return (
        <div className={styles.passwordInputWrapper}>
            <Input type={isPasswordVisible ? "text" : "password"} {...props}/>
            <button type="button"
                    title={t.form.password_show}
                    className={styles.eyeIcon}
                    onMouseDown={() => setIsPasswordVisible(true)}
                    onMouseUp={() => setIsPasswordVisible(false)}
                    onMouseLeave={() => setIsPasswordVisible(false)}
                    onKeyDown={e => handleKeyDown(e, true)}
                    onKeyUp={e => handleKeyDown(e, false)}
            >
                <FontAwesomeIcon icon={faEye}/>
            </button>
        </div>
    );
};

export default InputPassword;
