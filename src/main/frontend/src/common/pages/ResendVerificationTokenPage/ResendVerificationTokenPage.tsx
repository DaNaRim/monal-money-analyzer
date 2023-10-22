import { useForm } from "react-hook-form";
import useFetchUtils, { type FormSystemFields } from "../../../app/hooks/formUtils";
import useTranslation from "../../../app/hooks/translation";
import usePageTitle from "../../../app/hooks/usePageTitle";
import AppMessageComp from "../../../features/appMessages/AppMessageComp";
import { AppMessageType } from "../../../features/appMessages/appMessagesSlice";
import {
    useResendVerificationTokenMutation,
} from "../../../features/registration/registrationApiSlice";
import Form from "../../components/form/Form/Form";
import InputEmail from "../../components/form/InputEmail/InputEmail";
import styles from "./ResendVerificationTokenPage.module.scss";

type ResendVerificationTokenFields = FormSystemFields & {
    email: string;
};

const COMPONENT_NAME = "resendVerificationEmailPage";

const ResendVerificationTokenPage = () => {
    usePageTitle(COMPONENT_NAME);

    const t = useTranslation();

    const { handleResponseError } = useFetchUtils();

    const {
        register,
        handleSubmit,
        setError,
        formState: { errors },
    } = useForm<ResendVerificationTokenFields>();

    const [resendToken, { isLoading, isSuccess }] = useResendVerificationTokenMutation();

    const handleResendToken = (data: ResendVerificationTokenFields) => {
        resendToken(data.email).unwrap()
            .catch(e => handleResponseError(e, setError));
    };

    return (
        <main className={styles.resend_verification_token_page}
              data-testid="resend-verification-token-page">
            <h1>{t.resendVerificationEmailPage.title}</h1>
            {isSuccess && <AppMessageComp type={AppMessageType.INFO}
                                          messageCode="email_resend_success"
                                          page="resendVerificationEmail"/>
            }
            <Form onSubmit={handleSubmit(handleResendToken)}
                  componentName={COMPONENT_NAME}
                  isSubmitting={isLoading}
                  {...{ register, errors }}>

                <InputEmail name="email"
                            options={{ required: true }}
                            componentName={COMPONENT_NAME}
                            {...{ register, errors }}
                />
                <button type="submit">{t.resendVerificationEmailPage.form.submit}</button>
            </Form>
        </main>
    );
};

export default ResendVerificationTokenPage;
