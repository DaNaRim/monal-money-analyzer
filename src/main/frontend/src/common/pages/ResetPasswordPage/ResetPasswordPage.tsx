import React from "react";
import {useForm} from "react-hook-form";
import useFetchUtils, {FormSystemFields} from "../../../app/hooks/formUtils";
import useTranslation from "../../../app/hooks/translation";
import {useResetPasswordMutation} from "../../../features/registration/registrationApiSlice";
import styles from "./ResetPasswordPage.module.scss";


type ResetPasswordFields = FormSystemFields & {
    email: string;
}

const ResetPasswordPage = () => {
    const t = useTranslation();

    const {handleResponseError} = useFetchUtils();

    const {register, handleSubmit, setError, formState: {errors}} = useForm<ResetPasswordFields>();

    const [resetPassword, {isLoading, isSuccess}] = useResetPasswordMutation();

    const handleResetPassword = (data: ResetPasswordFields) => {
        resetPassword(data.email).unwrap()
            .catch(e => handleResponseError(e, setError));
    };

    return (
        <main className={styles.reset_password_page}>
            <h1>{t.resetPasswordPage.title}</h1>
            {isSuccess && <span className={`${styles.app_message} ${styles.info}`}>{t.resetPasswordPage.success}</span>}

            <form onSubmit={handleSubmit(handleResetPassword)}>
                <label htmlFor="email">{t.resetPasswordPage.form.fields.email}</label>
                <input type="text" id="email" {...register("email", {required: true})}/><br/>
                {errors.email?.type === "required" && <span>{t.resetPasswordPage.form.errors.email.required}</span>}
                {errors.email && <span>{errors.email.message}</span>}<br/>

                <input type="hidden" {...register("globalError")}/>
                {errors.globalError && <span>{errors.globalError.message}</span>}<br/>

                <input type="hidden" {...register("serverError")}/>
                {errors.serverError && <span className={styles.server_error}>{errors.serverError.message}</span>}
                <br/>

                {isLoading
                    ? <span>{t.resetPasswordPage.form.loading}</span>
                    : <button type="submit">{t.resetPasswordPage.form.submit}</button>
                }
            </form>
        </main>
    );
};

export default ResetPasswordPage;
