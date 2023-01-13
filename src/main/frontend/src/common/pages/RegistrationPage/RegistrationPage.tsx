import React from "react";
import {useForm} from "react-hook-form";
import {RegistrationDto, useRegisterMutation} from "../../../features/registration/registrationApiSlice";
import PageWrapper from "../../components/pageComponents/PageWrapper/PageWrapper";
import {clearFormSystemFields, FormSystemFields, handleResponseError} from "../../utils/FormUtils";
import styles from "./RegistrationPage.module.scss";


type RegistrationFormFields = FormSystemFields & RegistrationDto;

const RegistrationPage = () => {
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
        <PageWrapper>
            <main className={styles.registration_page}>
                <h1>Registration Page</h1>
                {isSuccess &&
                  <span className={`${styles.app_message} ${styles.info}`}>
                        Registration successful. Please check your email to activate your account. If it doesn't appear
                        within a few minutes, check your spam folder.
                  </span>
                }
                <form onSubmit={handleSubmit(handleRegistration)}>
                    <label htmlFor="firstName">FirstName: </label>
                    <input type="text" id="firstName" {...register("firstName", {required: true})}/><br/>
                    {errors.firstName?.type === "required" && <span>First name is required</span>}
                    {errors.firstName && <span>{errors.firstName.message}</span>}<br/>

                    <label htmlFor="lastName">LastName: </label>
                    <input type="text" id="lastName" {...register("lastName", {required: true})}/><br/>
                    {errors.lastName?.type === "required" && <span>Last name is required</span>}
                    {errors.lastName && <span>{errors.lastName.message}</span>}<br/>

                    <label htmlFor="email">Email: </label>
                    <input type="email" id="email" {...register("email", {required: true})}/><br/>
                    {errors.email?.type === "required" && <span>Email is required</span>}
                    {errors.email && <span>{errors.email.message}</span>}<br/>

                    <label htmlFor="password">Password: </label>
                    <input type="password" id="password" {...register("password", {required: true})}/><br/>
                    {errors.password?.type === "required" && <span>Password is required</span>}
                    {errors.password && <span>{errors.password.message}</span>}<br/>

                    <label htmlFor="matchingPassword">MatchingPassword: </label>
                    <input type="password"
                           id="matchingPassword" {...register("matchingPassword", {required: true})}/><br/>
                    {errors.matchingPassword?.type === "required" && <span>Matching password is required</span>}

                    <input type="hidden" {...register("globalError")}/>
                    {errors.globalError && <span>{errors.globalError.message}</span>}<br/>

                    <input type="hidden" {...register("serverError")}/>
                    {errors.serverError
                        && <span className={styles.server_error}>{errors.serverError.message}</span>
                    }<br/>

                    {isLoading
                        ? <span>Loading...</span>
                        : <button type="submit">Register</button>
                    }
                </form>
            </main>
        </PageWrapper>
    );
};

export default RegistrationPage;
