import { useForm } from "react-hook-form";
import { useNavigate } from "react-router";
import { useAppDispatch } from "../../../app/hooks/reduxHooks";
import useTranslation from "../../../app/hooks/translation";
import usePageTitle from "../../../app/hooks/usePageTitle";
import { ROUTE_LOGIN } from "../../../app/routes";
import {
    addAppMessage,
    AppMessageCode,
    AppMessageType,
} from "../../../features/appMessages/appMessagesSlice";
import {
    type ResetPasswordDto,
    useResetPasswordSetMutation,
} from "../../../features/registration/registrationApiSlice";
import Form from "../../components/form/Form/Form";
import InputPassword from "../../components/form/InputPassword/InputPassword";
import {
    clearFormSystemFields,
    type FormSystemFields,
    handleResponseError,
} from "../../utils/formUtils";
import styles from "./ResetPasswordSetPage.module.scss";

type ResetPasswordSetFields = FormSystemFields & ResetPasswordDto;

const COMPONENT_NAME = "resetPasswordSetPage";

const ResetPasswordSetPage = () => {
    usePageTitle(COMPONENT_NAME);

    const t = useTranslation();
    const dispatch = useAppDispatch();
    const navigate = useNavigate();

    const {
        register,
        handleSubmit,
        setValue,
        setError,
        formState: { errors },
    } = useForm<ResetPasswordSetFields>();

    const [resetPasswordSetReq, { isLoading }] = useResetPasswordSetMutation();

    const handleResetPasswordSet = (data: ResetPasswordSetFields) => {
        clearFormSystemFields(data);

        resetPasswordSetReq(data).unwrap()
            .then(() => dispatch(addAppMessage({
                type: AppMessageType.INFO,
                page: "login",
                messageCode: AppMessageCode.PASSWORD_RESET_SUCCESS,
            })))
            .then(() => navigate(ROUTE_LOGIN))
            .catch(e => {
                setValue("newPassword", "");
                setValue("matchingPassword", "");
                handleResponseError(e, setError);
            });
    };

    return (
        <main className={styles.reset_password_set_page} data-testid="reset-password-set-page">
            <h1>{t.resetPasswordSetPage.title}</h1>
            <Form onSubmit={handleSubmit(handleResetPasswordSet)}
                  componentName={COMPONENT_NAME}
                  isSubmitting={isLoading}
                  {...{ register, errors }}>

                <InputPassword name="newPassword"
                               options={{ required: true }}
                               componentName={COMPONENT_NAME}
                               {...{ register, errors }}
                />
                <InputPassword name="matchingPassword"
                               options={{ required: true }}
                               componentName={COMPONENT_NAME}
                               {...{ register, errors }}
                />
                <button type="submit">{t.resetPasswordSetPage.form.submit}</button>
            </Form>
        </main>
    );
};

export default ResetPasswordSetPage;
