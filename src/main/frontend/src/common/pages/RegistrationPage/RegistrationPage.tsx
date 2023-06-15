import React from "react";
import { useForm } from "react-hook-form";
import useFetchUtils, { type FormSystemFields } from "../../../app/hooks/formUtils";
import useTranslation from "../../../app/hooks/translation";
import {
    type RegistrationDto,
    useRegisterMutation,
} from "../../../features/registration/registrationApiSlice";
import ErrorGlobal from "../../components/form/ErrorGlobal/ErrorGlobal";
import ErrorServer from "../../components/form/ErrorServer/ErrorServer";
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
            {isSuccess &&
              <span className={`${styles.app_message} ${styles.info}`}>
                  {t.registerPage.success}
              </span>
            }
            <form onSubmit={handleSubmit(handleRegistration)}>

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
                <ErrorGlobal {...{ register, errors }}/>
                <ErrorServer {...{ register, errors }}/>

                {isLoading
                    ? <span>{t.registerPage.form.loading}</span>
                    : <button type="submit" data-testid="register-button">
                        {t.registerPage.form.submit}
                    </button>
                }
            </form>
        </main>
    );
};

export default RegistrationPage;
