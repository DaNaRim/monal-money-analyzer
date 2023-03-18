import React from "react";
import {useForm} from "react-hook-form";
import {useNavigate} from "react-router";
import useFetchUtils, {FormSystemFields} from "../../../app/hooks/formUtils";
import {useAppDispatch} from "../../../app/hooks/reduxHooks";
import useTranslation from "../../../app/hooks/translation";
import {addAppMessage, AppMessageCode, AppMessageType} from "../../../features/appMessages/appMessagesSlice";
import {ResetPasswordDto, useResetPasswordSetMutation} from "../../../features/registration/registrationApiSlice";
import ErrorGlobal from "../../components/form/ErrorGlobal/ErrorGlobal";
import ErrorServer from "../../components/form/ErrorServer/ErrorServer";
import InputPassword from "../../components/form/InputPassword/InputPassword";
import styles from "./ResetPasswordSetPage.module.scss";


type ResetPasswordSetFields = FormSystemFields & ResetPasswordDto;

const COMPONENT_NAME = "resetPasswordSetPage";

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
                page: "login",
                messageCode: AppMessageCode.PASSWORD_RESET_SUCCESS,
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

                <InputPassword name="newPassword"
                               options={{required: true}}
                               componentName={COMPONENT_NAME}
                               {...{register, errors}}
                />
                <InputPassword name="matchingPassword"
                               options={{required: true}}
                               componentName={COMPONENT_NAME}
                               {...{register, errors}}
                />
                <ErrorGlobal {...{register, errors}}/>
                <ErrorServer {...{register, errors}}/>

                {isLoading
                    ? <span>{t.resetPasswordSetPage.form.loading}</span>
                    : <button type="submit">{t.resetPasswordSetPage.form.submit}</button>
                }
            </form>
        </main>
    );
};

export default ResetPasswordSetPage;
