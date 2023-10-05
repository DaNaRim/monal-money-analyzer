import { MenuItem } from "@mui/material";
import dayjs from "dayjs";
import React, { useEffect, useState } from "react";
import { type Control, type UseFormHandleSubmit, type UseFormSetValue } from "react-hook-form";
import { type FieldErrors } from "react-hook-form/dist/types/errors";
import { type UseFormRegister } from "react-hook-form/dist/types/form";
import { useAppSelector } from "../../../app/hooks/reduxHooks";
import useTranslation from "../../../app/hooks/translation";
import {
    type Category,
    selectCategoriesWithSubcategories,
} from "../../../features/category/categorySlice";
import { getCategoryLocalName } from "../../../features/category/categoryUtil";
import { selectWallets, WALLET_BALANCE_MAX_VALUE } from "../../../features/wallet/walletSlice";
import ErrorGlobal from "../../components/form/ErrorGlobal/ErrorGlobal";
import ErrorServer from "../../components/form/ErrorServer/ErrorServer";
import InputButton from "../../components/form/InputButton/InputButton";
import InputDateTime from "../../components/form/InputDateTime/InputDateTime";
import InputNumber from "../../components/form/InputNumber/InputNumber";
import InputSelect from "../../components/form/InputSelect/InputSelect";
import InputTextarea from "../../components/form/InputTextarea/InputTextarea";
import {
    BASIC_CURRENCY_PRECISION,
    getCurrencyTypePrecision,
    type PossiblePrecision,
} from "../../utils/currencyList";
import SelectCategoryModal from "../SelectCategoryModal/SelectCategoryModal";
import { type TransactionFormFields } from "./CreateTransactionModal";
import styles from "./CreateTransactionModal.module.scss";

interface CreateUpdateTransactionFormProps {
    mode: "create" | "update";

    register: UseFormRegister<TransactionFormFields>;
    control: Control<TransactionFormFields>;
    errors: FieldErrors<TransactionFormFields>;
    getValues: () => TransactionFormFields;
    setValue: UseFormSetValue<TransactionFormFields>;

    handleSubmit: UseFormHandleSubmit<TransactionFormFields>;
    isLoading: boolean;

    handleFormSubmit: (data: TransactionFormFields) => void;
    walletId: number;
    date: string;
}

/**
 * Form for creating or updating transaction
 *
 * @param mode - form mode: create or update
 * @param register - react-hook-form register function
 * @param control - react-hook-form control function
 * @param errors - react-hook-form errors object
 * @param getValues - react-hook-form getValues function
 * @param setValue - react-hook-form setValue function
 * @param handleSubmit - react-hook-form handleSubmit function
 * @param isLoading - form loading state
 * @param handleFormSubmit - form submit handler
 * @param walletId - wallet id for which transaction is created or updated
 * @param date - default date for transaction. Format: YYYY-MM-DD
 */
const CreateUpdateTransactionForm = ({
                                         mode,
                                         register,
                                         control,
                                         errors,
                                         getValues,
                                         setValue,
                                         handleSubmit,
                                         isLoading,
                                         handleFormSubmit,
                                         walletId,
                                         date,
                                     }: CreateUpdateTransactionFormProps) => {
    const componentName = mode === "create"
        ? "createTransactionModal"
        : "updateTransactionModal";

    const t = useTranslation();

    const [amountPrecision, setAmountPrecision]
        = useState<PossiblePrecision>(BASIC_CURRENCY_PRECISION);

    const wallets = useAppSelector(selectWallets);

    const categories = useAppSelector(selectCategoriesWithSubcategories);

    const [selectCategoryModalOpen, setSelectCategoryModalOpen] = useState<boolean>(false);

    const [selectedCategory, setSelectedCategory] = useState<Category | undefined>(undefined);

    const getAvailableWallets = () => {
        if (mode === "update") {
            const transactionCurrency = wallets.find(wallet => wallet.id === walletId)?.currency;

            return wallets.filter(wallet => wallet.currency === transactionCurrency);
        }
        return wallets;
    };

    useEffect(() => {
        const categoryId = getValues().categoryId;

        if (categoryId != null) {
            setSelectedCategory(categories.find(category => category.id === categoryId));
        }
    }, [open]);

    useEffect(() => {
        if (selectedCategory == null) {
            return;
        }
        setValue("categoryId", selectedCategory?.id);
    }, [selectedCategory]);

    // Set the default date in form to selected date
    useEffect(() => {
        if (mode === "create") {
            setValue("date", dayjs(date)
                .add(dayjs().hour(), "hour")
                .add(dayjs().minute(), "minute")
                .format("YYYY-MM-DDTHH:mm"));
            return;
        }
        setValue("date", dayjs(date).format("YYYY-MM-DDTHH:mm"));
    }, [date]);

    // Set amount precision to selected wallet currency precision
    useEffect(() => {
        setAmountPrecision(getCurrencyTypePrecision(
            wallets.find(wallet => wallet.id === walletId)?.currency,
        ));
    }, [walletId]);

    return (
        <>
            <h1 className={styles.title}>{t[`${componentName}`].title}</h1>
            <form className={styles.form} onSubmit={handleSubmit(handleFormSubmit)}>
                <div className={styles.double_field}>
                    <InputSelect
                        name="walletId"
                        defaultValue={walletId}
                        componentName={componentName}
                        options={{
                            required: true,
                            disabled: mode === "update" && getAvailableWallets().length === 1,
                        }}
                        onChange={walletId => {
                            setAmountPrecision(getCurrencyTypePrecision(
                                wallets.find(wallet => wallet.id === walletId)?.currency,
                            ));
                        }}
                        renderValue={id => <p>{wallets.find(wallet => wallet.id === id)?.name}</p>}
                        {...{ control, errors, setValue }}
                    >
                        {getAvailableWallets().map(wallet => (
                            <MenuItem key={wallet.id} value={wallet.id}>
                                <p>{wallet.name}</p>
                            </MenuItem>
                        ))}
                    </InputSelect>

                    <InputButton name="categoryId"
                                 componentName={componentName}
                                 isRequired={true}
                                 displayValue={selectedCategory === undefined
                                     ? undefined
                                     : getCategoryLocalName(selectedCategory, categories, t)
                                 }
                                 label={t[`${componentName}`].form.fields.categoryId}
                                 onClick={() => setSelectCategoryModalOpen(true)}
                                 {...{ register, errors }}
                    />
                </div>
                <div className={styles.double_field}>
                    <InputDateTime name="date"
                                   componentName={componentName}
                                   options={{
                                       required: true,
                                       min: "2000-01-01",
                                       max: dayjs().add(1, "day").format("YYYY-MM-DD"),
                                   }}
                                   {...{ register, errors }}
                    />
                    <InputNumber name="amount"
                                 componentName={componentName}
                                 options={{ required: true }}
                                 step={amountPrecision}
                                 max={WALLET_BALANCE_MAX_VALUE}
                                 min={0}
                                 {...{ register, errors }}
                    />
                </div>
                <InputTextarea name="description"
                               componentName={componentName}
                               {...{ control, errors }}
                />
                <ErrorGlobal {...{ register, errors }}/>
                <ErrorServer {...{ register, errors }}/>

                {isLoading
                    ? <span>{t[`${componentName}`].form.loading}</span>
                    : <button className={styles.submit_button} type="submit">
                        {t[`${componentName}`].form.submit}
                    </button>
                }
            </form>
            <SelectCategoryModal open={selectCategoryModalOpen}
                                 setOpen={setSelectCategoryModalOpen}
                                 selectedCategory={selectedCategory}
                                 setCategory={setSelectedCategory}/>
        </>
    );
};

export default CreateUpdateTransactionForm;
