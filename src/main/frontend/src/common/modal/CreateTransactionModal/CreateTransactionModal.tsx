import { Box, Fade, Modal } from "@mui/material";
import React, { type Dispatch, type SetStateAction, useEffect } from "react";
import { useForm } from "react-hook-form";
import useFetchUtils, { type FormSystemFields } from "../../../app/hooks/formUtils";
import { useAppDispatch, useAppSelector } from "../../../app/hooks/reduxHooks";
import AppMessageComp from "../../../features/appMessages/AppMessageComp";
import { AppMessageType } from "../../../features/appMessages/appMessagesSlice";
import {
    CategoryType,
    selectCategoriesWithSubcategories,
} from "../../../features/category/categorySlice";
import { useCreateTransactionMutation } from "../../../features/transaction/transactionApiSlice";
import {
    addTransaction,
    type CreateTransactionDto,
    type Transaction,
} from "../../../features/transaction/transactionSlice";
import { updateWalletBalance } from "../../../features/wallet/walletSlice";
import CreateTransactionForm from "./CreateTransactionForm";
import styles from "./CreateTransactionModal.module.scss";

interface CreateTransactionModalProps {
    open: boolean;
    setOpen: Dispatch<SetStateAction<boolean>>;
    walletId: number;
    date: string;
}

export type CreateTransactionFormFields = CreateTransactionDto & FormSystemFields;

const CreateTransactionModal = ({ open, setOpen, walletId, date }: CreateTransactionModalProps) => {
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
                                                 walletId={walletId}
                                                 date={date}/>
                    }
                </Box>
            </Fade>
        </Modal>
    );
};

export default CreateTransactionModal;
