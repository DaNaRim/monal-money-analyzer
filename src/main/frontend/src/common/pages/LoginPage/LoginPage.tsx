import React, {useState} from "react";
import {useForm} from "react-hook-form";
import {useNavigate} from "react-router";
import {Link} from "react-router-dom";
import useFetchUtils, {ErrorResponse, FormSystemFields} from "../../../app/hooks/formUtils";
import {useAppDispatch, useAppSelector} from "../../../app/hooks/reduxHooks";
import useTranslation from "../../../app/hooks/translation";
import {AppMessageType, deleteAppMessage, selectAppMessages} from "../../../features/appMessages/appMessagesSlice";
import {Credentials, useLoginMutation} from "../../../features/auth/authApiSlice";
import {selectAuthIsForceLogin, setCredentials, setForceLogin} from "../../../features/auth/authSlice";
import styles from "./LoginPage.module.scss";


type LoginFormFields = FormSystemFields & Credentials

type LoginFormError = ErrorResponse & {
    fieldName: keyof LoginFormFields,
}

const LoginPage = () => {
    const dispatch = useAppDispatch();
    const navigate = useNavigate();
    const t = useTranslation();

    const {handleResponseError, clearFormSystemFields} = useFetchUtils();

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

            return <Link to="/resendVerificationToken">{t.loginPage.resendVerificationEmail}</Link>;
        }
        return null;
    };

    return (
        <main className={styles.login_page}>
            <h1>{t.loginPage.title}</h1>
            {appMessage && <p className={getAppMessageClassName(appMessage.type)}>{appMessage.message}</p>}
            {suggestResendVerificationToken()}

            <form onSubmit={handleSubmit(handleLogin)}>
                <label htmlFor="username">{t.loginPage.form.fields.email}</label>
                <input type="email" id="username" {...register("username", {required: true})}/>
                {errors.username?.type === "required" && <span>{t.loginPage.form.errors.email.required}</span>}
                {errors.username && <span>{errors.username.message}</span>}<br/>

                <label htmlFor="password">{t.loginPage.form.fields.password}</label>
                <input type="password" id="password" {...register("password", {required: true})}/>
                {errors.password?.type === "required" && <span>{t.loginPage.form.errors.password.required}</span>}
                {errors.password && <span>{errors.password.message}</span>}<br/>

                <input type="hidden" {...register("globalError")}/>
                {errors.globalError && <span>{errors.globalError.message}</span>}<br/>
                {isAccountNotActivated
                    && <Link to={"/resendVerificationToken"}>{t.loginPage.resendVerificationEmail}</Link>}

                <input type="hidden" {...register("serverError")}/>
                {errors.serverError && <span className={styles.server_error}>{errors.serverError.message}</span>}
                <br/>

                {isLoading
                    ? <span>{t.loginPage.form.loading}</span>
                    : <button type="submit">{t.loginPage.form.submit}</button>
                }
                <Link to={"/resetPassword"}>{t.loginPage.form.forgotPassword}</Link>
            </form>
        </main>
    );
};

export default LoginPage;
