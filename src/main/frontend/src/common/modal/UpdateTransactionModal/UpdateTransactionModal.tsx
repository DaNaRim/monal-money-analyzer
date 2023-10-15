import { Fade, Modal } from "@mui/material";
import React, { type Dispatch, type SetStateAction, useEffect } from "react";
import { useForm } from "react-hook-form";
import useFetchUtils from "../../../app/hooks/formUtils";
import { useAppDispatch, useAppSelector } from "../../../app/hooks/reduxHooks";
import {
    CategoryType,
    selectCategoriesWithSubcategories,
} from "../../../features/category/categorySlice";
import { useUpdateTransactionMutation } from "../../../features/transaction/transactionApiSlice";
import {
    type Transaction,
    updateTransaction,
    type UpdateTransactionDto,
    type ViewTransactionDto,
} from "../../../features/transaction/transactionSlice";
import { updateWalletBalance } from "../../../features/wallet/walletSlice";
import { type TransactionFormFields } from "../CreateTransactionModal/CreateTransactionModal";
import styles from "../CreateTransactionModal/CreateTransactionModal.module.scss";
import CreateUpdateTransactionForm from "../CreateTransactionModal/CreateUpdateTransactionForm";

interface EditTransactionModalProps {
    open: boolean;
    setOpen: Dispatch<SetStateAction<boolean>>;
    transaction: Transaction;
}

/**
 * Modal for editing existing transaction
 *
 * @param open - modal open state
 * @param setOpen - modal open state setter
 * @param transaction - transaction to edit
 */
const UpdateTransactionModal = ({ open, setOpen, transaction }: EditTransactionModalProps) => {
    const dispatch = useAppDispatch();

    const { handleResponseError, clearFormSystemFields } = useFetchUtils();

    const categoriesWithSubcategories = useAppSelector(selectCategoriesWithSubcategories);

    const [updateTransactionMutation, { isLoading }] = useUpdateTransactionMutation();

    const {
        control,
        register,
        handleSubmit,
        getValues,
        setValue,
        setError,
        reset: resetForm,
        clearErrors,
        formState: { errors },
    } = useForm<TransactionFormFields>();

    const handleSubmitForm = (data: TransactionFormFields) => {
        clearFormSystemFields(data);

        const updateTransactionDto: UpdateTransactionDto = {
            ...data,
            id: transaction.id,
        };
        // Can happen when wallet input is disabled
        if (updateTransactionDto.walletId === undefined) {
            updateTransactionDto.walletId = transaction.walletId;
        }
        updateTransactionMutation(updateTransactionDto).unwrap()
            .then(res => {
                const category = categoriesWithSubcategories
                    .find(category => category.id === res.categoryId) ?? null;

                const newTransaction: Transaction = {
                    ...res,
                    category,
                };
                dispatch(updateTransaction(newTransaction));

                // Update wallet balance if wallet, category type or amount changes
                if (category?.type !== transaction.category?.type
                    || res.amount !== transaction.amount
                    || res.walletId !== transaction.walletId) {
                    changeWalletBalance(res);
                }
                handleClose();
                resetForm();
            })
            .catch(err => handleResponseError(err, setError));
    };

    const handleClose = () => {
        clearErrors();
        setOpen(false);
    };

    const changeWalletBalance = (newTransaction: ViewTransactionDto) => {
        const category = categoriesWithSubcategories
            .find(category => category.id === newTransaction.categoryId) ?? null;

        const transactionOldDeltaAmount = transaction.category?.type === CategoryType.INCOME
            ? transaction.amount
            : -transaction.amount;
        const transactionNewDeltaAmount = category?.type === CategoryType.INCOME
            ? newTransaction.amount
            : -newTransaction.amount;

        if (transaction.walletId === newTransaction.walletId) { // wallet isn't changed
            dispatch(updateWalletBalance({
                walletId: transaction.walletId,
                deltaBalance: transactionNewDeltaAmount - transactionOldDeltaAmount,
                categoryType: CategoryType.INCOME,
            }));
        } else { // wallet changed
            dispatch(updateWalletBalance({
                walletId: transaction.walletId,
                deltaBalance: transactionOldDeltaAmount,
                categoryType: CategoryType.OUTCOME,
            }));
            dispatch(updateWalletBalance({
                walletId: newTransaction.walletId,
                deltaBalance: transactionNewDeltaAmount,
                categoryType: CategoryType.INCOME,
            }));
        }
    };

    useEffect(() => { // Fill form with transaction data
        // walletId and date set inside form
        if (transaction.category != null) {
            setValue("categoryId", transaction.category.id);
        }
        setValue("amount", transaction.amount);
        if (transaction.description != null) {
            setValue("description", transaction.description);
        }
    }, [transaction]);

    return (
        <Modal open={open} onClose={handleClose}>
            <Fade in={open}>
                <div className={styles.modal_block}>
                    <CreateUpdateTransactionForm mode={"update"}
                                                 register={register}
                                                 control={control}
                                                 errors={errors}
                                                 getValues={getValues}
                                                 setValue={setValue}
                                                 handleSubmit={handleSubmit}
                                                 isLoading={isLoading}
                                                 handleFormSubmit={handleSubmitForm}
                                                 walletId={transaction.walletId}
                                                 date={transaction.date}/>
                </div>
            </Fade>
        </Modal>
    );
};

export default UpdateTransactionModal;
