import { useForm } from "react-hook-form";
import useTranslation from "../../../app/hooks/translation";
import usePageTitle from "../../../app/hooks/usePageTitle";
import AppMessageComp from "../../../features/appMessages/AppMessageComp";
import { AppMessageType } from "../../../features/appMessages/appMessagesSlice";
import { useResetPasswordMutation } from "../../../features/registration/registrationApiSlice";
import Form from "../../components/form/Form/Form";
import InputEmail from "../../components/form/InputEmail/InputEmail";
import { type FormSystemFields, handleResponseError } from "../../utils/formUtils";
import styles from "./ResetPasswordPage.module.scss";

type ResetPasswordFields = FormSystemFields & {
    email: string;
};

const COMPONENT_NAME = "resetPasswordPage";

const ResetPasswordPage = () => {
    usePageTitle(COMPONENT_NAME);

    const t = useTranslation();

    const {
        register,
        handleSubmit,
        setError,
        formState: { errors },
    } = useForm<ResetPasswordFields>();

    const [resetPassword, { isLoading, isSuccess }] = useResetPasswordMutation();

    const handleResetPassword = async (data: ResetPasswordFields) => {
        return await resetPassword(data.email).unwrap()
            .catch(e => handleResponseError(e, setError));
    };

    return (
        <main className={styles.reset_password_page} data-testid="reset-password-page">
            <h1>{t.resetPasswordPage.title}</h1>
            {isSuccess && <AppMessageComp type={AppMessageType.INFO}
                                          messageCode="reset_password_success"
                                          page="resetPassword"/>
            }
            <Form onSubmit={handleSubmit(handleResetPassword)}
                  componentName={COMPONENT_NAME}
                  isSubmitting={isLoading}
                  {...{ register, errors }}>

                <InputEmail name="email"
                            options={{ required: true }}
                            componentName={COMPONENT_NAME}
                            {...{ register, errors }}
                />
                <button type="submit">{t.resetPasswordPage.form.submit}</button>
            </Form>
        </main>
    );
};

export default ResetPasswordPage;
