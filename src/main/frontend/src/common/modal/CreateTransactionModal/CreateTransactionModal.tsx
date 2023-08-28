import { Box, Fade, MenuItem, Modal } from "@mui/material";
import dayjs from "dayjs";
import React, { type Dispatch, type SetStateAction, useEffect, useState } from "react";
import {
    type Control,
    useForm,
    type UseFormHandleSubmit,
    type UseFormSetValue,
} from "react-hook-form";
import { type FieldErrors } from "react-hook-form/dist/types/errors";
import { type UseFormRegister } from "react-hook-form/dist/types/form";
import useFetchUtils, { type FormSystemFields } from "../../../app/hooks/formUtils";
import { useAppDispatch, useAppSelector } from "../../../app/hooks/reduxHooks";
import useTranslation from "../../../app/hooks/translation";
import AppMessageComp from "../../../features/appMessages/AppMessageComp";
import { AppMessageType } from "../../../features/appMessages/appMessagesSlice";
import {
    type Category,
    CategoryType,
    selectCategoriesWithSubcategories,
} from "../../../features/category/categorySlice";
import { useCreateTransactionMutation } from "../../../features/transaction/transactionApiSlice";
import {
    addTransaction,
    type CreateTransactionDto,
    type Transaction,
} from "../../../features/transaction/transactionSlice";
import {
    selectWallets,
    updateWalletBalance,
    WALLET_BALANCE_MAX_VALUE,
    WALLET_BALANCE_PRECISION_VALUE,
} from "../../../features/wallet/walletSlice";
import ErrorGlobal from "../../components/form/ErrorGlobal/ErrorGlobal";
import ErrorServer from "../../components/form/ErrorServer/ErrorServer";
import InputButton from "../../components/form/InputButton/InputButton";
import InputDateTime from "../../components/form/InputDateTime/InputDateTime";
import InputNumber from "../../components/form/InputNumber/InputNumber";
import InputSelect from "../../components/form/InputSelect/InputSelect";
import InputTextarea from "../../components/form/InputTextarea/InputTextarea";
import SelectCategoryModal from "../SelectCategoryModal/SelectCategoryModal";
import styles from "./CreateTransactionModal.module.scss";

interface CreateTransactionModalProps {
    open: boolean;
    setOpen: Dispatch<SetStateAction<boolean>>;
    walletId: number;
}

type CreateTransactionFormFields = CreateTransactionDto & FormSystemFields;

const COMPONENT_NAME = "createTransactionModal";

const CreateTransactionModal = ({ open, setOpen, walletId }: CreateTransactionModalProps) => {
    const dispatch = useAppDispatch();

    const { handleResponseError, clearFormSystemFields } = useFetchUtils();

    const categoriesWithSubcategories = useAppSelector(selectCategoriesWithSubcategories);

    const [
        createTransaction, {
            isLoading,
            isSuccess,
            reset: resetMutation,
        },
    ] = useCreateTransactionMutation();

    const {
        control,
        register,
        handleSubmit,
        setValue,
        setError,
        reset: resetForm,
        clearErrors,
        formState: { errors },
    } = useForm<CreateTransactionFormFields>();

    const handleSubmitForm = (data: CreateTransactionFormFields) => {
        clearFormSystemFields(data);

        createTransaction(data).unwrap()
            .then(res => {
                const category = categoriesWithSubcategories
                    .find(category => category.id === res.categoryId) ?? null;

                const newTransaction: Transaction = {
                    ...res,
                    category,
                };
                dispatch(addTransaction({ transaction: newTransaction, walletId: data.walletId }));

                dispatch(updateWalletBalance({
                    walletId: data.walletId,
                    deltaBalance: res.amount,
                    categoryType: category?.type ?? CategoryType.INCOME,
                }));

                setTimeout(() => {
                    handleClose();
                    resetForm();
                }, 1_500);
            }).catch(err => handleResponseError(err, setError));
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
                                          messageCode="transaction_create_success"
                                          page="transactions"
                                          className={styles.appMessage}
                        />
                        : <CreateTransactionForm register={register}
                                                 control={control}
                                                 errors={errors}
                                                 setValue={setValue}
                                                 handleSubmit={handleSubmit}
                                                 isLoading={isLoading}
                                                 handleCreateTransaction={handleSubmitForm}
                                                 walletId={walletId}/>
                    }
                </Box>
            </Fade>
        </Modal>
    );
};

export default CreateTransactionModal;

interface CreateTransactionFormProps {
    register: UseFormRegister<CreateTransactionFormFields>;
    control: Control<CreateTransactionFormFields>;
    errors: FieldErrors<CreateTransactionFormFields>;
    setValue: UseFormSetValue<CreateTransactionFormFields>;

    handleSubmit: UseFormHandleSubmit<CreateTransactionFormFields>;
    isLoading: boolean;

    handleCreateTransaction: (data: CreateTransactionFormFields) => void;
    walletId: number;
}

const CreateTransactionForm = ({
                                   register,
                                   control,
                                   errors,
                                   setValue,
                                   handleSubmit,
                                   isLoading,
                                   handleCreateTransaction,
                                   walletId,
                               }: CreateTransactionFormProps) => {
    const t = useTranslation();

    const wallets = useAppSelector(selectWallets);

    const [selectCategoryModalOpen, setSelectCategoryModalOpen] = useState<boolean>(false);

    const [selectedCategory, setSelectedCategory] = useState<Category | undefined>(undefined);

    const getCategoryLocalName = (category: Category | undefined) => {
        if (category == null) {
            return undefined;
        }
        const categoryNameKey = category.name.toLowerCase().replaceAll(" ", "_");

        return categoryNameKey == null
            ? t.data.transactionCategory.deleted
            : t.getString(
                `data.transactionCategory.${category.type.toLowerCase()}.${categoryNameKey}`,
            );
    };

    useEffect(() => {
        if (selectedCategory == null) {
            return;
        }
        setValue("categoryId", selectedCategory?.id);
    }, [selectedCategory]);

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
                                 value={selectedCategory?.id}
                                 displayValue={getCategoryLocalName(selectedCategory)}
                                 emptyText={t.createTransactionModal.form.fields.categoryId}
                                 onClick={() => setSelectCategoryModalOpen(true)}
                                 {...{ register, errors }}
                    />
                </div>
                <div className={styles.double_field}>
                    <InputDateTime name="date"
                                   componentName={COMPONENT_NAME}
                                   defaultValue={dayjs().format("YYYY-MM-DD HH:mm")}
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
                                 step={WALLET_BALANCE_PRECISION_VALUE}
                                 max={WALLET_BALANCE_MAX_VALUE}
                                 min={-WALLET_BALANCE_MAX_VALUE}
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
