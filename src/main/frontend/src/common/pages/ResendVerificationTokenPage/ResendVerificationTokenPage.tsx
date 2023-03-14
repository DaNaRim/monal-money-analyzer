import React from "react";
import {useForm} from "react-hook-form";
import useFetchUtils, {FormSystemFields} from "../../../app/hooks/formUtils";
import useTranslation from "../../../app/hooks/translation";
import {useResendVerificationTokenMutation} from "../../../features/registration/registrationApiSlice";
import styles from "./ResendVerificationTokenPage.module.scss";


type ResendVerificationTokenFields = FormSystemFields & {
    email: string;
}

const ResendVerificationTokenPage = () => {
    const t = useTranslation();

    const {handleResponseError} = useFetchUtils();

    const {register, handleSubmit, setError, formState: {errors}} = useForm<ResendVerificationTokenFields>();

    const [resendToken, {isLoading, isSuccess}] = useResendVerificationTokenMutation();

    const handleResendToken = (data: ResendVerificationTokenFields) => {
        resendToken(data.email).unwrap()
            .catch(e => handleResponseError(e, setError));
    };

    return (
        <main className={styles.login_page}>
            <h1>{t.resendVerificationEmailPage.title}</h1>
            {isSuccess &&
              <span className={`${styles.app_message} ${styles.info}`}>{t.resendVerificationEmailPage.success}</span>
            }
            <form onSubmit={handleSubmit(handleResendToken)}>
                <label htmlFor="email">{t.resendVerificationEmailPage.form.fields.email}</label>
                <input type="email" id="email" {...register("email", {required: true})}/>
                {errors.email?.type === "required"
                    && <span>{t.resendVerificationEmailPage.form.errors.email.required}</span>}
                {errors.email && <span>{errors.email.message}</span>}<br/>

                <input type="hidden" {...register("globalError")}/>
                {errors.globalError && <span>{errors.globalError.message}</span>}<br/>

                <input type="hidden" {...register("serverError")}/>
                {errors.serverError && <span className={styles.server_error}>{errors.serverError.message}</span>}
                <br/>

                {isLoading
                    ? <span>{t.resendVerificationEmailPage.form.loading}</span>
                    : <button type="submit">{t.resendVerificationEmailPage.form.submit}</button>
                }
            </form>
        </main>
    );
};

export default ResendVerificationTokenPage;
