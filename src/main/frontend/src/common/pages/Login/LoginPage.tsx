import React from "react";
import {useNavigate} from "react-router";
import {Credentials, useLoginMutation} from "../../../features/auth/authApiSlice";
import {useAppDispatch} from "../../../app/hooks";
import PageWrapper from "../../components/pageComponents/PageWrapper/PageWrapper";

import styles from "./LoginPage.module.scss";
import {useForm} from "react-hook-form";
import {setCredentials} from "../../../features/auth/authSlice";

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
    const {register, handleSubmit, setError, formState: {errors}} = useForm<LoginFormFields>();

    const [login, {isLoading}] = useLoginMutation();

    const navigate = useNavigate();
    const dispatch = useAppDispatch();

    const handleLogin = (data: LoginFormFields) => {
        delete data.globalError;
        delete data.serverError;

        login(data).unwrap()
            .then(data => dispatch(setCredentials(data)))
            .then(() => navigate(onSuccessRedirect))
            .catch(e => {
                if (e.status === 400) {
                    const errorData: GenericError[] = e.data;
                    errorData.forEach(error => setError(error.fieldName, {type: error.type, message: error.message}));
                } else {
                    setError("serverError", {
                        type: "serverError",
                        message: "Server unavailable. please try again later",
                    });
                }
            });
    };

    return (
        <PageWrapper>
            <main className={styles.login_page}>
                <h1>Login page</h1>
                <form onSubmit={handleSubmit(handleLogin)}>
                    <label htmlFor="username">Email: </label>
                    <input type="email" {...register("username", {required: true})}/>
                    {errors.username?.type === "required" && <span>Email is required</span>}
                    {errors.username && <span>{errors.username.message}</span>}<br/>

                    <label htmlFor="password">Password: </label>
                    <input type="password" {...register("password", {required: true})}/>
                    {errors.password?.type === "required" && <span>Password is required</span>}
                    {errors.password && <span>{errors.password.message}</span>}<br/>

                    <input type="hidden" {...register("globalError")}/>
                    {errors.globalError && <span>{errors.globalError.message}</span>}<br/>

                    <input type="hidden" {...register("serverError")}/>
                    {errors.serverError //TODO extract styles to separate file
                        && <span style={{fontSize: "2rem", color: "red"}}>{errors.serverError.message}</span>
                    }<br/>

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
