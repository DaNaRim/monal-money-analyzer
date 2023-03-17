import {faEye} from "@fortawesome/free-regular-svg-icons";
import {FontAwesomeIcon} from "@fortawesome/react-fontawesome";
import {useState} from "react";
import useTranslation from "../../../../app/hooks/translation";
import Input, {InputExtProps} from "../Input/Input";
import styles from "./InputPassword.module.scss";

const InputPassword = (props: InputExtProps) => {
    const t = useTranslation();

    const [isPasswordVisible, setIsPasswordVisible] = useState(false);

    const handlePasswordVisibility = () => setIsPasswordVisible(!isPasswordVisible);

    return (
        <div className={styles.passwordInputWrapper}>
            <Input type={isPasswordVisible ? "text" : "password"} {...props}/>
            <button type="button"
                    title={t.form.password_show}
                    className={styles.eyeIcon}
                    onMouseDown={handlePasswordVisibility}
                    onMouseUp={handlePasswordVisibility}>
                <FontAwesomeIcon icon={faEye}/>
            </button>
        </div>
    );
};

export default InputPassword;