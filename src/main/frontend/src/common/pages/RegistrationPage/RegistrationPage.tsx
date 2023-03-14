import React from "react";
import {useForm} from "react-hook-form";
import useFetchUtils, {FormSystemFields} from "../../../app/hooks/formUtils";
import useTranslation from "../../../app/hooks/translation";
import {RegistrationDto, useRegisterMutation} from "../../../features/registration/registrationApiSlice";
import styles from "./RegistrationPage.module.scss";


type RegistrationFormFields = FormSystemFields & RegistrationDto;

const RegistrationPage = () => {
    const t = useTranslation();

    const {handleResponseError, clearFormSystemFields} = useFetchUtils();

    const {register, handleSubmit, setValue, setError, formState: {errors}} = useForm<RegistrationFormFields>();

    const [registerReq, {isLoading, isSuccess}] = useRegisterMutation();

    const handleRegistration = (data: RegistrationFormFields) => {
        clearFormSystemFields(data);

        registerReq(data).unwrap()
            .catch(e => {
                setValue("password", "");
                setValue("matchingPassword", "");
                handleResponseError(e, setError);
            });
    };

    return (
        <main className={styles.registration_page}>
            <h1>{t.registerPage.title}</h1>
            {isSuccess && <span className={`${styles.app_message} ${styles.info}`}>{t.registerPage.success}</span>}

            <form onSubmit={handleSubmit(handleRegistration)}>
                <label htmlFor="firstName">{t.registerPage.form.fields.firstName}</label>
                <input type="text" id="firstName" {...register("firstName", {required: true})}/><br/>
                {errors.firstName?.type === "required" && <span>{t.registerPage.form.errors.firstName.required}</span>}
                {errors.firstName && <span>{errors.firstName.message}</span>}<br/>

                <label htmlFor="lastName">{t.registerPage.form.fields.lastName}</label>
                <input type="text" id="lastName" {...register("lastName", {required: true})}/><br/>
                {errors.lastName?.type === "required" && <span>{t.registerPage.form.errors.lastName.required}</span>}
                {errors.lastName && <span>{errors.lastName.message}</span>}<br/>

                <label htmlFor="email">{t.registerPage.form.fields.email}</label>
                <input type="email" id="email" {...register("email", {required: true})}/><br/>
                {errors.email?.type === "required" && <span>{t.registerPage.form.errors.email.required}</span>}
                {errors.email && <span>{errors.email.message}</span>}<br/>

                <label htmlFor="password">{t.registerPage.form.fields.password}</label>
                <input type="password" id="password" {...register("password", {required: true})}/><br/>
                {errors.password?.type === "required" && <span>{t.registerPage.form.errors.password.required}</span>}
                {errors.password && <span>{errors.password.message}</span>}<br/>

                <label htmlFor="matchingPassword">{t.registerPage.form.fields.confirmPassword}</label>
                <input type="password"
                       id="matchingPassword" {...register("matchingPassword", {required: true})}/><br/>
                {errors.matchingPassword?.type === "required"
                    && <span>{t.registerPage.form.errors.confirmPassword.required}</span>}

                <input type="hidden" {...register("globalError")}/>
                {errors.globalError && <span>{errors.globalError.message}</span>}<br/>

                <input type="hidden" {...register("serverError")}/>
                {errors.serverError
                    && <span className={styles.server_error}>{errors.serverError.message}</span>
                }<br/>

                {isLoading
                    ? <span>{t.registerPage.form.loading}</span>
                    : <button type="submit">{t.registerPage.form.submit}</button>
                }
            </form>
        </main>
    );
};

export default RegistrationPage;
