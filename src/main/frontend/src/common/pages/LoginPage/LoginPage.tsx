import React, {useState} from "react";
import {useForm} from "react-hook-form";
import {useNavigate} from "react-router";
import {Link} from "react-router-dom";
import {useAppDispatch, useAppSelector} from "../../../app/hooks";
import {AppMessageType, deleteAppMessage, selectAppMessages} from "../../../features/appMessages/appMessagesSlice";
import {Credentials, useLoginMutation} from "../../../features/auth/authApiSlice";
import {selectAuthIsForceLogin, setCredentials, setForceLogin} from "../../../features/auth/authSlice";
import {clearFormSystemFields, ErrorResponse, FormSystemFields, handleResponseError} from "../../utils/FormUtils";
import styles from "./LoginPage.module.scss";


type LoginFormFields = FormSystemFields & Credentials

type LoginFormError = ErrorResponse & {
    fieldName: keyof LoginFormFields,
}

const LoginPage = () => {
    const dispatch = useAppDispatch();
    const navigate = useNavigate();

    const {register, handleSubmit, setValue, setError, formState: {errors}} = useForm<LoginFormFields>();

    const appMessage = useAppSelector(selectAppMessages).find(msg => msg.page === "login");
    const isForceLogin = useAppSelector(selectAuthIsForceLogin);

    const [isAccountNotActivated, setIsAccountNotActivated] = useState<boolean>(false);

    const [login, {isLoading}] = useLoginMutation();

    const handleLogin = (data: LoginFormFields) => {
        clearFormSystemFields(data);

        login(data).unwrap()
            .then(data => dispatch(setCredentials(data)))
            .then(() => {
                if (appMessage) {
                    dispatch(deleteAppMessage(appMessage.messageCode));
                }
            })
            .then(() => {
                if (isForceLogin) {
                    dispatch(setForceLogin(false));
                    navigate(-1);
                } else {
                    navigate("/");
                }
            })
            .catch(e => {
                setValue("password", "");

                const errorData: LoginFormError[] = e.data;

                if (typeof errorData === "object"
                    && errorData.some(error => error.errorCode === "validation.auth.disabled")) {

                    setIsAccountNotActivated(true);
                }
                handleResponseError(e, setError);
            });
    };

    const getAppMessageClassName = (type: AppMessageType) => {
        const classMap = {
            "INFO": `${styles.app_message} ${styles.info}`,
            "WARNING": `${styles.app_message} ${styles.warn}`,
            "ERROR": `${styles.app_message} ${styles.error}`,
        };
        return classMap[type];
    };

    const suggestResendVerificationToken = () => {
        if (appMessage?.messageCode === "validation.token.verification.not-found"
            || appMessage?.messageCode === "validation.token.verification.expired") {

            return <Link to="/resendVerificationToken">Resend verification token</Link>;
        }
        return null;
    };

    return (
        <main className={styles.login_page}>
            <h1>Login page</h1>
            {appMessage && <p className={getAppMessageClassName(appMessage.type)}>{appMessage.message}</p>}
            {suggestResendVerificationToken()}

            <form onSubmit={handleSubmit(handleLogin)}>
                <label htmlFor="username">Email: </label>
                <input type="email" id="username" {...register("username", {required: true})}/>
                {errors.username?.type === "required" && <span>Email is required</span>}
                {errors.username && <span>{errors.username.message}</span>}<br/>

                <label htmlFor="password">Password: </label>
                <input type="password" id="password" {...register("password", {required: true})}/>
                {errors.password?.type === "required" && <span>Password is required</span>}
                {errors.password && <span>{errors.password.message}</span>}<br/>

                <input type="hidden" {...register("globalError")}/>
                {errors.globalError && <span>{errors.globalError.message}</span>}<br/>
                {isAccountNotActivated && <Link to={"/resendVerificationToken"}>Resend verification token</Link>}

                <input type="hidden" {...register("serverError")}/>
                {errors.serverError && <span className={styles.server_error}>{errors.serverError.message}</span>}
                <br/>

                {isLoading
                    ? <span>Loading...</span>
                    : <button type="submit">Login</button>
                }
                <Link to={"/resetPassword"}>Forgot password?</Link>
            </form>
        </main>
    );
};

export default LoginPage;
