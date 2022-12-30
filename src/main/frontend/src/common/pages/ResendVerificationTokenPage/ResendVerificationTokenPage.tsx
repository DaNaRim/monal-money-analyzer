import PageWrapper from "@components/pageComponents/PageWrapper/PageWrapper";
import {useResendVerificationTokenMutation} from "@features/registration/registrationApiSlice";
import {FormSystemFields, handleResponseError} from "@utils/FormUtils";
import React from "react";
import {useForm} from "react-hook-form";
import styles from "./ResendVerificationTokenPage.module.scss";


type ResendVerificationTokenFields = FormSystemFields & {
    email: string;
}

const ResendVerificationTokenPage = () => {
    const {register, handleSubmit, setError, formState: {errors}} = useForm<ResendVerificationTokenFields>();

    const [resendToken, {isLoading, isSuccess}] = useResendVerificationTokenMutation();

    const handleResendToken = (data: ResendVerificationTokenFields) => {
        resendToken(data.email).unwrap()
            .catch(e => handleResponseError(e, setError));
    };

    return (
        <PageWrapper>
            <main className={styles.login_page}>
                <h1>Resend verification token page</h1>
                {isSuccess &&
                  <span className={`${styles.app_message} ${styles.info}`}>
                    Check your email for a link to verify your account. If it doesn't appear within a few minutes,
                    check your spam folder.
                  </span>
                }
                <form onSubmit={handleSubmit(handleResendToken)}>
                    <label htmlFor="email">Email: </label>
                    <input type="email" id="email" {...register("email", {required: true})}/>
                    {errors.email?.type === "required" && <span>Email is required</span>}
                    {errors.email && <span>{errors.email.message}</span>}<br/>

                    <input type="hidden" {...register("globalError")}/>
                    {errors.globalError && <span>{errors.globalError.message}</span>}<br/>

                    <input type="hidden" {...register("serverError")}/>
                    {errors.serverError && <span className={styles.server_error}>{errors.serverError.message}</span>}
                    <br/>

                    {isLoading
                        ? <span>Loading...</span>
                        : <button type="submit">Resend token</button>
                    }
                </form>
            </main>
        </PageWrapper>
    );
};

export default ResendVerificationTokenPage;
