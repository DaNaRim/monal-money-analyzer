import { Autocomplete, Box } from "@mui/material";
import React, { useState } from "react";
import { type Control, Controller, type UseFormHandleSubmit } from "react-hook-form";
import { type FieldErrors } from "react-hook-form/dist/types/errors";
import { type UseFormRegister } from "react-hook-form/dist/types/form";
import useTranslation from "../../../app/hooks/translation";
import { WALLET_BALANCE_MAX_VALUE } from "../../../features/wallet/walletSlice";
import Form from "../../components/form/Form/Form";
import InputNumber from "../../components/form/InputNumber/InputNumber";
import InputText from "../../components/form/InputText/InputText";
import OptionInput from "../../components/form/OptionInput/OptionInput";
import currencyList, { getCurrencyTypePrecision } from "../../utils/currencyList";
import { type CreateWalletFormFields } from "./CreateWalletModal";
import styles from "./CreateWalletModal.module.scss";

interface CreateWalletFormProps {
    register: UseFormRegister<CreateWalletFormFields>;
    control: Control<CreateWalletFormFields>;
    errors: FieldErrors<CreateWalletFormFields>;

    handleSubmit: UseFormHandleSubmit<CreateWalletFormFields>;
    isLoading: boolean;

    handleCreateWallet: (data: CreateWalletFormFields) => void;
}

const componentName = "createWalletModal";

const CreateWalletForm = ({
                              register,
                              control,
                              errors,
                              handleSubmit,
                              isLoading,
                              handleCreateWallet,
                          }: CreateWalletFormProps) => {
    const t = useTranslation();

    const [walletBalancePrecision, setWalletBalancePrecision] = useState<0.01 | 0.00000001>(0.01);

    return (
        <>
            <h1>{t.createWalletModal.title}</h1>
            <Form onSubmit={handleSubmit(handleCreateWallet)}
                  componentName={componentName}
                  isSubmitting={isLoading}
                  {...{ register, errors }}>

                <InputText name="name"
                           options={{ required: true }}
                           componentName={componentName}
                           additionalProps={{ autoComplete: "off" }}
                           {...{ register, errors }}/>

                <div className={styles.double_field}>
                    <InputNumber name="balance"
                                 step={walletBalancePrecision}
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
                                onChange={(_, data) => {
                                    controllerProps.field.onChange(data);
                                    setWalletBalancePrecision(getCurrencyTypePrecision(data));
                                }}
                                onBlur={controllerProps.field.onBlur}
                                options={currencyList}
                                getOptionLabel={option => option}
                                fullWidth={true}
                                disableClearable={true}
                                noOptionsText={t.form.no_options_text}
                                data-testid="autocomplete-currency"
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
                <button className={styles.submit_button} type="submit">
                    {t.createWalletModal.form.submit}
                </button>
            </Form>
        </>
    );
};

export default CreateWalletForm;
