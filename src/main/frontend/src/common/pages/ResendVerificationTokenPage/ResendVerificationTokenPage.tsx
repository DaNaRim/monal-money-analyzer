import React from "react";
import {useForm} from "react-hook-form";
import useFetchUtils, {FormSystemFields} from "../../../app/hooks/formUtils";
import useTranslation from "../../../app/hooks/translation";
import {useResendVerificationTokenMutation} from "../../../features/registration/registrationApiSlice";
import ErrorGlobal from "../../components/form/ErrorGlobal/ErrorGlobal";
import ErrorServer from "../../components/form/ErrorServer/ErrorServer";
import InputEmail from "../../components/form/InputEmail/InputEmail";
import styles from "./ResendVerificationTokenPage.module.scss";


type ResendVerificationTokenFields = FormSystemFields & {
    email: string;
}

const COMPONENT_NAME = "resendVerificationEmailPage";

const ResendVerificationTokenPage = () => {
    const t = useTranslation();

    const {handleResponseError} = useFetchUtils();

    const {register, handleSubmit, setError, formState: {errors}} = useForm<ResendVerificationTokenFields>();

    const [resendToken, {isLoading, isSuccess}] = useResendVerificationTokenMutation();

    const handleResendToken = (data: ResendVerificationTokenFields) =>
        resendToken(data.email).unwrap()
            .catch(e => handleResponseError(e, setError));

    return (
        <main className={styles.login_page}>
            <h1>{t.resendVerificationEmailPage.title}</h1>
            {isSuccess &&
              <span className={`${styles.app_message} ${styles.info}`}>{t.resendVerificationEmailPage.success}</span>
            }
            <form onSubmit={handleSubmit(handleResendToken)}>

                <InputEmail name="email"
                            options={{required: true}}
                            componentName={COMPONENT_NAME}
                            {...{register, errors}}
                />
                <ErrorGlobal {...{register, errors}}/>
                <ErrorServer {...{register, errors}}/>

                {isLoading
                    ? <span>{t.resendVerificationEmailPage.form.loading}</span>
                    : <button type="submit">{t.resendVerificationEmailPage.form.submit}</button>
                }
            </form>
        </main>
    );
};

export default ResendVerificationTokenPage;
