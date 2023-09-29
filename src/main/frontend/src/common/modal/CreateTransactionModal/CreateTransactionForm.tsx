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
    selectTransactionCategories,
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
import { type CreateTransactionFormFields } from "./CreateTransactionModal";
import styles from "./CreateTransactionModal.module.scss";

interface CreateTransactionFormProps {
    register: UseFormRegister<CreateTransactionFormFields>;
    control: Control<CreateTransactionFormFields>;
    errors: FieldErrors<CreateTransactionFormFields>;
    setValue: UseFormSetValue<CreateTransactionFormFields>;

    handleSubmit: UseFormHandleSubmit<CreateTransactionFormFields>;
    isLoading: boolean;

    handleCreateTransaction: (data: CreateTransactionFormFields) => void;
    walletId: number;
    date: string;
}

const COMPONENT_NAME = "createTransactionModal";

const CreateTransactionForm = ({
                                   register,
                                   control,
                                   errors,
                                   setValue,
                                   handleSubmit,
                                   isLoading,
                                   handleCreateTransaction,
                                   walletId,
                                   date,
                               }: CreateTransactionFormProps) => {
    const t = useTranslation();

    const [amountPrecision, setAmountPrecision]
        = useState<PossiblePrecision>(BASIC_CURRENCY_PRECISION);

    const wallets = useAppSelector(selectWallets);

    const categories = useAppSelector(selectTransactionCategories);

    const [selectCategoryModalOpen, setSelectCategoryModalOpen] = useState<boolean>(false);

    const [selectedCategory, setSelectedCategory] = useState<Category | undefined>(undefined);

    useEffect(() => {
        if (selectedCategory == null) {
            return;
        }
        setValue("categoryId", selectedCategory?.id);
    }, [selectedCategory]);

    // Set the default date in form to selected date
    useEffect(() => {
        setValue("date", dayjs(date)
            .add(dayjs().hour(), "hour")
            .add(dayjs().minute(), "minute")
            .format("YYYY-MM-DDTHH:mm"));
    }, [date]);

    // Set amount precision to selected wallet currency precision
    useEffect(() => {
        setAmountPrecision(getCurrencyTypePrecision(
            wallets.find(wallet => wallet.id === walletId)?.currency,
        ));
    }, [walletId]);

    return (
        <>
            <h1 className={styles.title}>{t.createTransactionModal.title}</h1>
            <form className={styles.form} onSubmit={handleSubmit(handleCreateTransaction)}>
                <div className={styles.double_field}>
                    <InputSelect
                        name="walletId"
                        defaultValue={walletId}
                        componentName={COMPONENT_NAME}
                        options={{ required: true }}
                        onChange={walletId => {
                            setAmountPrecision(getCurrencyTypePrecision(
                                wallets.find(wallet => wallet.id === walletId)?.currency,
                            ));
                        }}
                        renderValue={id => <p>{wallets.find(wallet => wallet.id === id)?.name}</p>}
                        {...{ control, errors, setValue }}
                    >
                        {wallets.map(wallet => (
                            <MenuItem key={wallet.id} value={wallet.id}>
                                <p>{wallet.name}</p>
                            </MenuItem>
                        ))}
                    </InputSelect>

                    <InputButton name="categoryId"
                                 componentName={COMPONENT_NAME}
                                 isRequired={true}
                                 displayValue={
                                     getCategoryLocalName(
                                         selectedCategory ?? null,
                                         categories,
                                         t,
                                     )}
                                 label={t.createTransactionModal.form.fields.categoryId}
                                 onClick={() => setSelectCategoryModalOpen(true)}
                                 {...{ register, errors }}
                    />
                </div>
                <div className={styles.double_field}>
                    <InputDateTime name="date"
                                   componentName={COMPONENT_NAME}
                                   options={{
                                       required: true,
                                       min: "2000-01-01",
                                       max: dayjs().add(1, "day").format("YYYY-MM-DD"),
                                   }}
                                   {...{ register, errors }}
                    />
                    <InputNumber name="amount"
                                 componentName={COMPONENT_NAME}
                                 options={{ required: true }}
                                 step={amountPrecision}
                                 max={WALLET_BALANCE_MAX_VALUE}
                                 min={0}
                                 {...{ register, errors }}
                    />
                </div>
                <InputTextarea name="description"
                               componentName={COMPONENT_NAME}
                               {...{ control, errors }}
                />
                <ErrorGlobal {...{ register, errors }}/>
                <ErrorServer {...{ register, errors }}/>

                {isLoading
                    ? <span>{t.createTransactionModal.form.loading}</span>
                    : <button className={styles.submit_button} type="submit">
                        {t.createTransactionModal.form.submit}
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

export default CreateTransactionForm;
