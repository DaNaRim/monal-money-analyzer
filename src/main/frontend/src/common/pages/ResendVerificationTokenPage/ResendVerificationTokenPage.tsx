import React from "react";
import {useForm} from "react-hook-form";
import {Credentials} from "../../../features/auth/authApiSlice";
import {useResendVerificationTokenMutation} from "../../../features/registration/registrationApiSlice";
import PageWrapper from "../../components/pageComponents/PageWrapper/PageWrapper";
import styles from "../Login/LoginPage.module.scss";

interface ResendVerificationTokenFields extends Credentials {
    email: string;
    globalError?: string;
    serverError?: string;
}

type GenericError = {
    type: string,
    fieldName: "email" | "globalError" | "serverError",
    message: string
}

const ResendVerificationTokenPage = () => {
    const {register, handleSubmit, setError, formState: {errors}} = useForm<ResendVerificationTokenFields>();

    const [resendToken, {isLoading}] = useResendVerificationTokenMutation();

    const [successMessage, setSuccessMessage] = React.useState<string | null>(null);

    const handleResendToken = (data: ResendVerificationTokenFields) => {
        delete data.globalError;
        delete data.serverError;

        resendToken(data.email).unwrap()
            .then(() => setSuccessMessage("Verification token has been sent to your email"))
            .catch(e => {
                if (e.status === 401) {
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
                <h1>Resend verification token page</h1>
                {successMessage && <div>{successMessage}</div>}

                <form onSubmit={handleSubmit(handleResendToken)}>
                    <label htmlFor="username">Email: </label>
                    <input type="email" {...register("email", {required: true})}/>
                    {errors.username?.type === "required" && <span>Email is required</span>}
                    {errors.username && <span>{errors.username.message}</span>}<br/>

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
