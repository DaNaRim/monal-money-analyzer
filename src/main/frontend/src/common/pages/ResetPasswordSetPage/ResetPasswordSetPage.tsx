import React from "react";
import {useForm} from "react-hook-form";
import {useNavigate} from "react-router";
import useFetchUtils, {FormSystemFields} from "../../../app/hooks/formUtils";
import {useAppDispatch} from "../../../app/hooks/reduxHooks";
import useTranslation from "../../../app/hooks/translation";
import {addAppMessage, AppMessageType} from "../../../features/appMessages/appMessagesSlice";
import {ResetPasswordDto, useResetPasswordSetMutation} from "../../../features/registration/registrationApiSlice";
import styles from "./ResetPasswordSetPage.module.scss";


type ResetPasswordSetFields = FormSystemFields & ResetPasswordDto;

const ResetPasswordSetPage = () => {
    const dispatch = useAppDispatch();
    const navigate = useNavigate();
    const t = useTranslation();

    const {handleResponseError, clearFormSystemFields} = useFetchUtils();

    const {register, handleSubmit, setValue, setError, formState: {errors}} = useForm<ResetPasswordSetFields>();

    const [resetPasswordSetReq, {isLoading}] = useResetPasswordSetMutation();

    const handleResetPasswordSet = (data: ResetPasswordSetFields) => {
        clearFormSystemFields(data);

        resetPasswordSetReq(data).unwrap()
            .then(() => dispatch(addAppMessage({
                type: AppMessageType.INFO,
                message: t.resetPasswordSetPage.success,
                page: "login",
                messageCode: "message.password-reset.success",
            })))
            .then(() => navigate("/login"))
            .catch(e => {
                setValue("newPassword", "");
                setValue("matchingPassword", "");
                handleResponseError(e, setError);
            });
    };

    return (
        <main className={styles.reset_password_set_page}>
            <h1>{t.resetPasswordSetPage.title}</h1>

            <form onSubmit={handleSubmit(handleResetPasswordSet)}>
                <label htmlFor="newPassword">{t.resetPasswordSetPage.form.fields.newPassword}</label>
                <input type="password" id="newPassword" {...register("newPassword", {required: true})}/><br/>
                {errors.newPassword?.type === "required"
                    && <span>{t.resetPasswordSetPage.form.errors.newPassword.required}</span>}
                {errors.newPassword && <span>{errors.newPassword.message}</span>}<br/>

                <label htmlFor="matchingPassword">{t.resetPasswordSetPage.form.fields.confirmPassword}</label>
                <input type="password"
                       id="matchingPassword"
                       {...register("matchingPassword", {required: true})}/><br/>
                {errors.matchingPassword?.type === "required"
                    && <span>{t.resetPasswordSetPage.form.errors.confirmPassword.required}</span>}
                {errors.matchingPassword && <span>{errors.matchingPassword.message}</span>}<br/>

                <input type="hidden" {...register("globalError")}/>
                {errors.globalError && <span>{errors.globalError.message}</span>}<br/>

                <input type="hidden" {...register("serverError")}/>
                {errors.serverError && <span>{errors.serverError.message}</span>}<br/>

                {isLoading
                    ? <span>{t.resetPasswordSetPage.form.loading}</span>
                    : <button type="submit">{t.resetPasswordSetPage.form.submit}</button>
                }
            </form>
        </main>
    );
};

export default ResetPasswordSetPage;
