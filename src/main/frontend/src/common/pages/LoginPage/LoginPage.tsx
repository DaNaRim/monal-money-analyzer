import React, { useState } from "react";
import { useForm } from "react-hook-form";
import { useNavigate } from "react-router";
import { Link } from "react-router-dom";
import useFetchUtils, {
    type ErrorResponse,
    type FormSystemFields,
} from "../../../app/hooks/formUtils";
import { useAppDispatch, useAppSelector } from "../../../app/hooks/reduxHooks";
import useTranslation from "../../../app/hooks/translation";
import AppMessageEl from "../../../features/appMessages/AppMessageEl";
import {
    AppMessageCode,
    deleteAppMessage,
    selectAppMessages,
} from "../../../features/appMessages/appMessagesSlice";
import { type Credentials, useLoginMutation } from "../../../features/auth/authApiSlice";
import {
    selectAuthIsForceLogin,
    setCredentials,
    setForceLogin,
} from "../../../features/auth/authSlice";
import ErrorGlobal from "../../components/form/ErrorGlobal/ErrorGlobal";
import ErrorServer from "../../components/form/ErrorServer/ErrorServer";
import InputEmail from "../../components/form/InputEmail/InputEmail";
import InputPassword from "../../components/form/InputPassword/InputPassword";
import styles from "./LoginPage.module.scss";

type LoginFormFields = FormSystemFields & Credentials;

type LoginFormError = ErrorResponse & {
    fieldName: keyof LoginFormFields;
};

const COMPONENT_NAME = "loginPage";

const LoginPage = () => {
    const dispatch = useAppDispatch();
    const navigate = useNavigate();
    const t = useTranslation();

    const { handleResponseError, clearFormSystemFields } = useFetchUtils();

    const {
        register,
        handleSubmit,
        setValue,
        setError,
        formState: { errors },
    } = useForm<LoginFormFields>();

    const appMessage = useAppSelector(selectAppMessages).find(msg => msg.page === "login");
    const isForceLogin = useAppSelector(selectAuthIsForceLogin);

    const [isAccountNotActivated, setIsAccountNotActivated] = useState<boolean>(false);

    const [login, { isLoading }] = useLoginMutation();

    const handleLogin = (data: LoginFormFields) => {
        clearFormSystemFields(data);

        login(data).unwrap()
            .then(data => dispatch(setCredentials(data)))
            .then(() => {
                if (appMessage != null) {
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

    const suggestResendVerificationToken = () => {
        if (appMessage?.messageCode === AppMessageCode.TOKEN_VERIFICATION_NOT_FOUND
            || appMessage?.messageCode === AppMessageCode.TOKEN_VERIFICATION_EXPIRED) {
            return <Link to="/resendVerificationToken">{t.loginPage.resendVerificationEmail}</Link>;
        }
        return null;
    };

    return (
        <main className={styles.login_page}>
            <h1>{t.loginPage.title}</h1>
            {(appMessage != null) &&
              <AppMessageEl {...appMessage}>{suggestResendVerificationToken()}</AppMessageEl>
            }
            <form onSubmit={handleSubmit(handleLogin)}>

                <InputEmail name="username"
                            options={{ required: true }}
                            componentName={COMPONENT_NAME}
                            {...{ register, errors }}
                />
                <InputPassword name="password"
                               options={{ required: true }}
                               componentName={COMPONENT_NAME}
                               {...{ register, errors }}
                />
                <ErrorGlobal {...{ register, errors }}/>
                {isAccountNotActivated &&
                  <Link to={"/resendVerificationToken"}>
                      {t.loginPage.resendVerificationEmail}
                  </Link>
                }
                <ErrorServer {...{ register, errors }}/>

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
