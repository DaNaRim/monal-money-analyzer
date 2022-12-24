import React from "react";
import {useForm} from "react-hook-form";
import {useResetPasswordMutation} from "../../../features/registration/registrationApiSlice";
import PageWrapper from "../../components/pageComponents/PageWrapper/PageWrapper";
import styles from "./ResetPasswordPage.module.scss";

interface ResetPasswordFields {
    email: string;
    globalError?: string;
    serverError?: string;
}

type GenericError = {
    type: string,
    fieldName: "email" | "globalError" | "serverError",
    message: string
}

const ResetPasswordPage = () => {
    const {register, handleSubmit, setError, formState: {errors}} = useForm<ResetPasswordFields>();

    const [resetPassword, {isLoading, isSuccess}] = useResetPasswordMutation();

    const handleResetPassword = (data: ResetPasswordFields) => {
        delete data.globalError;
        delete data.serverError;

        resetPassword(data.email).unwrap()
            .catch(e => {
                if (e.status === 400) {
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

    return (
        <PageWrapper>
            <main className={styles.reset_password_page}>
                <h1>Reset Password Page</h1>
                {isSuccess &&
                  <span className={`${styles.app_message} ${styles.info}`}>
                    Check your email for a link to reset your password. If it doesn't appear within a few minutes,
                    check your spam folder.
                  </span>
                }
                <form onSubmit={handleSubmit(handleResetPassword)}>
                    <label htmlFor="email">Email: </label>
                    <input type="text" id="email" {...register("email", {required: true})}/><br/>
                    {errors.email?.type === "required" && <span>Email is required</span>}
                    {errors.email && <span>{errors.email.message}</span>}<br/>

                    <input type="hidden" {...register("globalError")}/>
                    {errors.globalError && <span>{errors.globalError.message}</span>}<br/>

                    <input type="hidden" {...register("serverError")}/>
                    {errors.serverError && <span className={styles.server_error}>{errors.serverError.message}</span>}
                    <br/>

                    {isLoading
                        ? <span>Loading...</span>
                        : <button type="submit">Reset password</button>
                    }
                </form>
            </main>
        </PageWrapper>
    );
};

export default ResetPasswordPage;
