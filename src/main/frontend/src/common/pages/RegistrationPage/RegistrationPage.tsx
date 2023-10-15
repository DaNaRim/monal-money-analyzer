import React from "react";
import { useForm } from "react-hook-form";
import useFetchUtils, { type FormSystemFields } from "../../../app/hooks/formUtils";
import useTranslation from "../../../app/hooks/translation";
import AppMessageComp from "../../../features/appMessages/AppMessageComp";
import { AppMessageType } from "../../../features/appMessages/appMessagesSlice";
import {
    type RegistrationDto,
    useRegisterMutation,
} from "../../../features/registration/registrationApiSlice";
import Form from "../../components/form/Form/Form";
import InputEmail from "../../components/form/InputEmail/InputEmail";
import InputPassword from "../../components/form/InputPassword/InputPassword";
import InputText from "../../components/form/InputText/InputText";
import styles from "./RegistrationPage.module.scss";

type RegistrationFormFields = FormSystemFields & RegistrationDto;

const COMPONENT_NAME = "registerPage";

const RegistrationPage = () => {
    const t = useTranslation();

    const { handleResponseError, clearFormSystemFields } = useFetchUtils();

    const {
        register,
        handleSubmit,
        setValue,
        setError,
        formState: { errors },
    } = useForm<RegistrationFormFields>();

    const [registerReq, { isLoading, isSuccess }] = useRegisterMutation();

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
        <main className={styles.registration_page} data-testid="registration-page">
            <h1>{t.registerPage.title}</h1>
            {isSuccess && <AppMessageComp type={AppMessageType.INFO}
                                          messageCode="registration_success"
                                          page="register"/>
            }
            <Form onSubmit={handleSubmit(handleRegistration)}
                  componentName={COMPONENT_NAME}
                  isSubmitting={isLoading}
                  {...{ register, errors }}>

                <InputText name="firstName"
                           options={{ required: true }}
                           componentName={COMPONENT_NAME}
                           {...{ register, errors }}
                />
                <InputText name="lastName"
                           options={{ required: true }}
                           componentName={COMPONENT_NAME}
                           {...{ register, errors }}
                />
                <InputEmail name="email"
                            options={{ required: true }}
                            componentName={COMPONENT_NAME}
                            {...{ register, errors }}
                />
                <InputPassword name="password"
                               options={{ required: true }}
                               componentName={COMPONENT_NAME}
                               {...{ register, errors }}
                />
                <InputPassword name="matchingPassword"
                               options={{ required: true }}
                               componentName={COMPONENT_NAME}
                               {...{ register, errors }}
                />
                <button type="submit" data-testid="register-button">
                    {t.registerPage.form.submit}
                </button>
            </Form>
        </main>
    );
};

export default RegistrationPage;
