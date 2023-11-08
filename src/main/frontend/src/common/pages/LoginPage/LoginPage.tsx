import { useEffect, useState } from "react";
import { useForm } from "react-hook-form";
import { useNavigate } from "react-router";
import { Link } from "react-router-dom";
import { useAppDispatch, useAppSelector } from "../../../app/hooks/reduxHooks";
import useTranslation from "../../../app/hooks/translation";
import usePageTitle from "../../../app/hooks/usePageTitle";
import {
    ROUTE_RESEND_VERIFICATION_TOKEN,
    ROUTE_RESET_PASSWORD,
    ROUTE_TRANSACTIONS,
} from "../../../app/routes";
import AppMessageComp from "../../../features/appMessages/AppMessageComp";
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
import Form from "../../components/form/Form/Form";
import InputEmail from "../../components/form/InputEmail/InputEmail";
import InputPassword from "../../components/form/InputPassword/InputPassword";
import {
    clearFormSystemFields,
    type ErrorResponse,
    type FormSystemFields,
    handleResponseError,
} from "../../utils/formUtils";
import styles from "./LoginPage.module.scss";

type LoginFormFields = FormSystemFields & Credentials;

type LoginFormError = ErrorResponse & {
    fieldName: keyof LoginFormFields;
};

const COMPONENT_NAME = "loginPage";

const LoginPage = () => {
    usePageTitle(COMPONENT_NAME);

    const t = useTranslation();
    const dispatch = useAppDispatch();
    const navigate = useNavigate();

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
                    navigate(-1); // Previous page
                } else {
                    navigate(ROUTE_TRANSACTIONS);
                }
            })
            .catch(e => {
                setValue("password", "");

                const errorData: LoginFormError[] = e.data;

                if (typeof errorData === "object" && errorData !== null
                    && errorData.some(error => error.errorCode === "validation.auth.disabled")) {
                    setIsAccountNotActivated(true);
                }
                handleResponseError(e, setError);
            });
    };

    const suggestResendVerificationToken = () => {
        if (appMessage?.messageCode === AppMessageCode.TOKEN_VERIFICATION_NOT_FOUND
            || appMessage?.messageCode === AppMessageCode.TOKEN_VERIFICATION_EXPIRED) {
            return <Link to={ROUTE_RESEND_VERIFICATION_TOKEN}>
                {t.loginPage.resendVerificationEmail}
            </Link>;
        }
        return null;
    };

    useEffect(() => {
        return () => { // unmount
            dispatch(setForceLogin(false));
            if (appMessage != null) {
                dispatch(deleteAppMessage(appMessage.messageCode));
            }
        };
    }, []);

    return (
        <main className={styles.login_page} data-testid="login-page">
            <h1>{t.loginPage.title}</h1>
            {(appMessage != null) &&
              <AppMessageComp {...appMessage}>{suggestResendVerificationToken()}</AppMessageComp>
            }
            <Form onSubmit={handleSubmit(handleLogin)}
                  componentName={COMPONENT_NAME}
                  isSubmitting={isLoading}
                  {...{ register, errors }}>

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
                {isAccountNotActivated &&
                  <Link to={ROUTE_RESEND_VERIFICATION_TOKEN}>
                      {t.loginPage.resendVerificationEmail}
                  </Link>
                }
                <button type="submit">{t.loginPage.form.submit}</button>
                <Link to={ROUTE_RESET_PASSWORD}>{t.loginPage.form.forgotPassword}</Link>
            </Form>
        </main>
    );
};

export default LoginPage;
