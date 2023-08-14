import { Autocomplete, Box, Fade, Modal } from "@mui/material";
import React, { useEffect } from "react";
import { type Control, Controller, useForm, type UseFormHandleSubmit } from "react-hook-form";
import { type FieldErrors } from "react-hook-form/dist/types/errors";
import { type UseFormRegister } from "react-hook-form/dist/types/form";
import useFetchUtils, { type FormSystemFields } from "../../../app/hooks/formUtils";
import { useAppDispatch } from "../../../app/hooks/reduxHooks";
import useTranslation from "../../../app/hooks/translation";
import AppMessageComp from "../../../features/appMessages/AppMessageComp";
import { AppMessageType } from "../../../features/appMessages/appMessagesSlice";
import { useCreateWalletMutation } from "../../../features/wallet/walletApiSlice";
import {
    addUserWallet,
    type CreateWalletDto,
    WALLET_BALANCE_MAX_VALUE,
    WALLET_BALANCE_PRECISION_VALUE,
} from "../../../features/wallet/walletSlice";
import ErrorGlobal from "../../components/form/ErrorGlobal/ErrorGlobal";
import ErrorServer from "../../components/form/ErrorServer/ErrorServer";
import InputNumber from "../../components/form/InputNumber/InputNumber";
import InputText from "../../components/form/InputText/InputText";
import OptionInput from "../../components/form/OptionInput/OptionInput";
import styles from "./CreateWalletModal.module.scss";
import currencyList from "./currencyList";

interface CreateWalletModalProps {
    open: boolean;
    setOpen: React.Dispatch<React.SetStateAction<boolean>>;
    setNewWalletId: React.Dispatch<React.SetStateAction<number | null>>;
}

type CreateWalletFormFields = CreateWalletDto & FormSystemFields;

const componentName = "createWalletModal";

const CreateWalletModal = ({
                               open,
                               setOpen,
                               setNewWalletId,
                           }: CreateWalletModalProps) => {
    const dispatch = useAppDispatch();

    const { handleResponseError, clearFormSystemFields } = useFetchUtils();

    const [createWallet, { isLoading, isSuccess, reset: resetMutation }]
        = useCreateWalletMutation();

    const {
        control,
        register,
        handleSubmit,
        setError,
        reset: resetForm,
        clearErrors,
        formState: { errors },
    } = useForm<CreateWalletFormFields>();

    const handleCreateWallet = (data: CreateWalletFormFields) => {
        clearFormSystemFields(data);

        createWallet(data).unwrap()
            .then(res => {
                dispatch(addUserWallet(res));
                setNewWalletId(res.id); // Auto selects new wallet
                setTimeout(() => {
                    handleClose();
                    resetForm();
                }, 1_500);
            })
            .catch(e => handleResponseError(e, setError));
    };

    const handleClose = () => {
        clearErrors();
        setOpen(false);
    };

    // I don't know why, but resetMutation doesn't work inside handleCreateWallet, so I put it here
    useEffect(() => {
        if (isSuccess && !open) {
            setTimeout(() => resetMutation(), 200);
        }
    }, [open, isSuccess]);

    return (
        <Modal
            open={open}
            onClose={handleClose}
        >
            <Fade in={open}>
                <Box className={styles.modal_block}>
                    {isSuccess
                        ? <AppMessageComp type={AppMessageType.INFO}
                                          messageCode="wallet_create_success"
                                          page="transactions"
                        />
                        : <CreateWalletForm register={register}
                                            control={control}
                                            errors={errors}
                                            handleSubmit={handleSubmit}
                                            isLoading={isLoading}
                                            handleCreateWallet={handleCreateWallet}
                        />
                    }
                </Box>
            </Fade>
        </Modal>
    );
};

export default CreateWalletModal;

interface CreateWalletFormProps {
    register: UseFormRegister<CreateWalletFormFields>;
    control: Control<CreateWalletFormFields>;
    errors: FieldErrors<CreateWalletFormFields>;

    handleSubmit: UseFormHandleSubmit<CreateWalletFormFields>;
    isLoading: boolean;

    handleCreateWallet: (data: CreateWalletFormFields) => void;
}

const CreateWalletForm = ({
                              register,
                              control,
                              errors,
                              handleSubmit,
                              isLoading,
                              handleCreateWallet,
                          }: CreateWalletFormProps) => {
    const t = useTranslation();

    return (
        <>
            <h1>{t.createWalletModal.title}</h1>
            <form onSubmit={handleSubmit(handleCreateWallet)} data-testid="createWalletForm">
                <InputText name="name"
                           options={{ required: true }}
                           componentName={componentName}
                           additionalProps={{ autoComplete: "off" }}
                           {...{ register, errors }}/>

                <div className={styles.double_field}>
                    <InputNumber name="balance"
                                 step={WALLET_BALANCE_PRECISION_VALUE}
                                 max={WALLET_BALANCE_MAX_VALUE}
                                 min={-WALLET_BALANCE_MAX_VALUE}
                                 defaultValue={0}
                                 options={{ required: true }}
                                 componentName={componentName}
                                 {...{ register, errors }}/>
                    <Controller
                        name="currency"
                        rules={{ required: true }}
                        control={control}
                        render={(controllerProps) => (
                            <Autocomplete
                                onChange={(_, data) => controllerProps.field.onChange(data)}
                                onBlur={controllerProps.field.onBlur}
                                options={currencyList}
                                getOptionLabel={option => option}
                                fullWidth={true}
                                disableClearable={true}
                                data-testid={"autocomplete-currency"}
                                filterOptions={(options, params) => {
                                    const input = params.inputValue.toLowerCase();
                                    return options.filter(option => {
                                        const value = option.toLowerCase();
                                        const name = t.getString(`data.currency.${option}`);
                                        return value.includes(input)
                                            || (name ?? "").toLowerCase().includes(input);
                                    });
                                }
                                }
                                componentsProps={{
                                    paper: {
                                        sx: {
                                            width: "fit-content",
                                            maxWidth: "20em",
                                        },
                                    },
                                }}
                                renderOption={(props, option) => (
                                    <Box component="li" {...props} className={styles.option}>
                                        <p className={styles.option_value}>{option}</p>
                                        <p className={styles.option_description}>
                                            {t.getString(`data.currency.${option}`)}
                                        </p>
                                    </Box>
                                )}
                                renderInput={params => (
                                    <OptionInput name="currency"
                                                 ref0={params.InputProps.ref}
                                                 componentName={componentName}
                                                 errors={errors}
                                                 isRequired={true}
                                                 inputProps={{ ...params.inputProps }}
                                    />
                                )}
                            />)}
                    />
                </div>
                <ErrorGlobal {...{ register, errors }}/>
                <ErrorServer {...{ register, errors }}/>
                {isLoading
                    ? <span>{t.createWalletModal.form.loading}</span>
                    : <button className={styles.submit_button} type="submit">
                        {t.createWalletModal.form.submit}
                    </button>
                }
            </form>
        </>
    );
};
