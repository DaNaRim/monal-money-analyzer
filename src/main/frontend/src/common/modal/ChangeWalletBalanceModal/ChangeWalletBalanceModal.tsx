import { Fade, Modal } from "@mui/material";
import dayjs from "dayjs";
import { type Dispatch, type SetStateAction, useEffect } from "react";
import { useForm } from "react-hook-form";
import useFetchUtils, { type FormSystemFields } from "../../../app/hooks/formUtils";
import { useAppDispatch, useAppSelector } from "../../../app/hooks/reduxHooks";
import useTranslation from "../../../app/hooks/translation";
import { selectCategoriesWithSubcategories } from "../../../features/category/categorySlice";
import { useCreateTransactionMutation } from "../../../features/transaction/transactionApiSlice";
import {
    addTransaction,
    type CreateTransactionDto,
    type Transaction,
} from "../../../features/transaction/transactionSlice";
import { updateWalletBalance, type Wallet } from "../../../features/wallet/walletSlice";
import Form from "../../components/form/Form/Form";
import InputNumber from "../../components/form/InputNumber/InputNumber";
import styles from "./ChangeWalletBalanceModal.module.scss";

interface ChangeWalletBalanceModalProps {
    open: boolean;
    setOpen: Dispatch<SetStateAction<boolean>>;
    wallet: Wallet | null;
}

type ChangeWalletBalanceModalFormFields = FormSystemFields & {
    balance: number;
};

const ChangeWalletBalanceModal = ({ open, setOpen, wallet }: ChangeWalletBalanceModalProps) => {
    const t = useTranslation();
    const dispatch = useAppDispatch();

    const [createTransaction, { isLoading }] = useCreateTransactionMutation();

    const { clearFormSystemFields, handleResponseError } = useFetchUtils();

    const categories = useAppSelector(selectCategoriesWithSubcategories);

    const {
        register,
        handleSubmit,
        setValue,
        clearErrors,
        setError,
        formState: { errors },
    } = useForm<ChangeWalletBalanceModalFormFields>();

    const handleUpdateBalance = (data: ChangeWalletBalanceModalFormFields) => {
        if (wallet == null) { // Should never happen
            return;
        }
        clearFormSystemFields(data);

        const amount = data.balance - wallet.balance;
        const category = amount > 0
            ? categories.find(category => category.type === "INCOME" && category.name === "Other")
            : categories.find(category => category.type === "OUTCOME" && category.name === "Other");

        if (category == null) {
            setError("globalError", { message: t.changeWalletBalanceModal.error });
            return;
        }
        const transaction: CreateTransactionDto = {
            walletId: wallet.id,
            date: dayjs().format("YYYY-MM-DD HH:mm"),
            amount: Math.abs(amount),
            categoryId: category?.id,
            description: t.changeWalletBalanceModal.transactionDescription,
        };
        createTransaction(transaction).unwrap()
            .then(res => {
                const newTransaction: Transaction = {
                    ...res,
                    category: categories
                        .find(category => category.id === res.categoryId) ?? null,
                };
                dispatch(addTransaction({ walletId: wallet.id, transaction: newTransaction }));

                dispatch(updateWalletBalance({
                    walletId: wallet.id,
                    deltaBalance: res.amount,
                    categoryType: category.type,
                }));
                handleClose();
            })
            .catch((error) => handleResponseError(error, setError));
    };

    const handleClose = () => {
        clearErrors();
        setOpen(false);
    };

    useEffect(() => {
        if (wallet == null) {
            return;
        }
        setValue("balance", wallet.balance);
    }, [wallet, open]);

    return (
        <Modal open={open} onClose={handleClose}>
            <Fade in={open}>
                <div className={styles.modal_block}>
                    <h2>{t.changeWalletBalanceModal.title}</h2>
                    <Form onSubmit={handleSubmit(handleUpdateBalance)}
                          componentName={"changeWalletBalanceModal"}
                          isSubmitting={isLoading}
                          {...{ register, errors }}>

                        <InputNumber name={"balance"}
                                     options={{ required: true }}
                                     componentName={"changeWalletBalanceModal"}
                                     {...{ register, errors }}/>

                        <button type="submit">{t.changeWalletBalanceModal.submit}</button>
                    </Form>
                </div>
            </Fade>
        </Modal>
    );
};

export default ChangeWalletBalanceModal;
