import React from "react";
import {useForm} from "react-hook-form";
import {useNavigate} from "react-router";
import {Link} from "react-router-dom";
import {useAppDispatch, useAppSelector} from "../../../app/hooks";
import {AppMessageType, deleteAppMessage, selectAppMessages} from "../../../features/appMessages/appMessagesSlice";
import {Credentials, useLoginMutation} from "../../../features/auth/authApiSlice";
import {setCredentials} from "../../../features/auth/authSlice";
import PageWrapper from "../../components/pageComponents/PageWrapper/PageWrapper";
import styles from "./LoginPage.module.scss";

interface LoginFormFields extends Credentials {
    globalError?: string;
    serverError?: string;
}

type GenericError = {
    type: string,
    fieldName: "username" | "password" | "globalError" | "serverError",
    message: string
}

const onSuccessRedirect: string = "/";

const LoginPage = () => {
    const dispatch = useAppDispatch();
    const navigate = useNavigate();

    const {register, handleSubmit, setError, formState: {errors}} = useForm<LoginFormFields>();

    const [login, {isLoading}] = useLoginMutation();

    const appMessage = useAppSelector(selectAppMessages).find(msg => msg.page === "login");

    const handleLogin = (data: LoginFormFields) => {
        delete data.globalError;
        delete data.serverError;

        login(data).unwrap()
            .then(data => dispatch(setCredentials(data)))
            .then(() => {
                if (appMessage) {
                    dispatch(deleteAppMessage(appMessage));
                }
            })
            .then(() => navigate(onSuccessRedirect))
            .catch(e => {
                if (e.status === 401) {
                    const errorData: GenericError[] = e.data;
                    errorData.forEach(error => setError(error.fieldName, {type: error.type, message: error.message}));
                } else if (e.status === "FETCH_ERROR" || e.status === 500) {
                    setError("serverError", {
                        type: "serverError",
                        message: "Server unavailable. please try again later",
                    });
                }
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

    return (
        <PageWrapper>
            <main className={styles.login_page}>
                <h1>Login page</h1>
                {appMessage && <p className={getAppMessageClassName(appMessage.type)}>{appMessage.message}</p>}
                {appMessage && appMessage.expectClientActionCode === "token.verification.resend"
                    && <Link to="/resendVerificationToken">Resend verification token</Link>
                }

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

                    <input type="hidden" {...register("serverError")}/>
                    {errors.serverError && <span className={styles.server_error}>{errors.serverError.message}</span>}
                    <br/>

                    {isLoading
                        ? <span>Loading...</span>
                        : <button type="submit">Login</button>
                    }
                </form>
            </main>
        </PageWrapper>
    );
};

export default LoginPage;
