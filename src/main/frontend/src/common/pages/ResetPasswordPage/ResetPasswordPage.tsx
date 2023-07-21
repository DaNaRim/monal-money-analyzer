import React from "react";
import { useForm } from "react-hook-form";
import useFetchUtils, { type FormSystemFields } from "../../../app/hooks/formUtils";
import useTranslation from "../../../app/hooks/translation";
import AppMessageComp from "../../../features/appMessages/AppMessageComp";
import { AppMessageType } from "../../../features/appMessages/appMessagesSlice";
import { useResetPasswordMutation } from "../../../features/registration/registrationApiSlice";
import ErrorGlobal from "../../components/form/ErrorGlobal/ErrorGlobal";
import ErrorServer from "../../components/form/ErrorServer/ErrorServer";
import InputEmail from "../../components/form/InputEmail/InputEmail";
import styles from "./ResetPasswordPage.module.scss";

type ResetPasswordFields = FormSystemFields & {
    email: string;
};

const COMPONENT_NAME = "resetPasswordPage";

const ResetPasswordPage = () => {
    const t = useTranslation();

    const { handleResponseError } = useFetchUtils();

    const {
        register,
        handleSubmit,
        setError,
        formState: { errors },
    } = useForm<ResetPasswordFields>();

    const [resetPassword, { isLoading, isSuccess }] = useResetPasswordMutation();

    const handleResetPassword = async (data: ResetPasswordFields) =>
        await resetPassword(data.email).unwrap()
            .catch(e => handleResponseError(e, setError));

    return (
        <main className={styles.reset_password_page} data-testid="reset-password-page">
            <h1>{t.resetPasswordPage.title}</h1>
            {isSuccess && <AppMessageComp type={AppMessageType.INFO}
                                          messageCode="reset_password_success"
                                          page="resetPassword"/>
            }
            <form onSubmit={handleSubmit(handleResetPassword)}>

                <InputEmail name="email"
                            options={{ required: true }}
                            componentName={COMPONENT_NAME}
                            {...{ register, errors }}
                />
                <ErrorGlobal {...{ register, errors }}/>
                <ErrorServer {...{ register, errors }}/>

                {isLoading
                    ? <span>{t.resetPasswordPage.form.loading}</span>
                    : <button type="submit">{t.resetPasswordPage.form.submit}</button>
                }
            </form>
        </main>
    );
};

export default ResetPasswordPage;
