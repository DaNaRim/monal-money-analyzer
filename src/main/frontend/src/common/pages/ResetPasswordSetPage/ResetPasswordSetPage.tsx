import React from "react";
import {useForm} from "react-hook-form";
import {useNavigate} from "react-router";
import {useAppDispatch} from "../../../app/hooks";
import {addAppMessage, AppMessageType} from "../../../features/appMessages/appMessagesSlice";
import {ResetPasswordDto, useResetPasswordSetMutation} from "../../../features/registration/registrationApiSlice";
import {clearFormSystemFields, FormSystemFields, handleResponseError} from "../../utils/FormUtils";
import styles from "./ResetPasswordSetPage.module.scss";


type ResetPasswordSetFields = FormSystemFields & ResetPasswordDto;

const ResetPasswordSetPage = () => {
    const dispatch = useAppDispatch();
    const navigate = useNavigate();

    const {register, handleSubmit, setValue, setError, formState: {errors}} = useForm<ResetPasswordSetFields>();

    const [resetPasswordSetReq, {isLoading}] = useResetPasswordSetMutation();

    const handleResetPasswordSet = (data: ResetPasswordSetFields) => {
        clearFormSystemFields(data);

        resetPasswordSetReq(data).unwrap()
            .then(() => dispatch(addAppMessage({
                type: AppMessageType.INFO,
                message: "Password reset successful. You can now login with your new password.",
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
            <h1>Set new password</h1>

            <form onSubmit={handleSubmit(handleResetPasswordSet)}>
                <label htmlFor="newPassword">New password: </label>
                <input type="password" id="newPassword" {...register("newPassword", {required: true})}/><br/>
                {errors.newPassword?.type === "required" && <span>New password is required</span>}
                {errors.newPassword && <span>{errors.newPassword.message}</span>}<br/>

                <label htmlFor="matchingPassword">Confirm password: </label>
                <input type="password"
                       id="matchingPassword"
                       {...register("matchingPassword", {required: true})}/><br/>
                {errors.matchingPassword?.type === "required" && <span>Confirm password is required</span>}
                {errors.matchingPassword && <span>{errors.matchingPassword.message}</span>}<br/>

                <input type="hidden" {...register("globalError")}/>
                {errors.globalError && <span>{errors.globalError.message}</span>}<br/>

                <input type="hidden" {...register("serverError")}/>
                {errors.serverError && <span>{errors.serverError.message}</span>}<br/>

                {isLoading
                    ? <span>loading...</span>
                    : <button type="submit">Set new password</button>
                }
            </form>
        </main>
    );
};

export default ResetPasswordSetPage;
