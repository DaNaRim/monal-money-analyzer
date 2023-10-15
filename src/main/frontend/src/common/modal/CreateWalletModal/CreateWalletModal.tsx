import { Fade, Modal } from "@mui/material";
import React, { useEffect } from "react";
import { useForm } from "react-hook-form";
import useFetchUtils, { type FormSystemFields } from "../../../app/hooks/formUtils";
import { useAppDispatch } from "../../../app/hooks/reduxHooks";
import AppMessageComp from "../../../features/appMessages/AppMessageComp";
import { AppMessageType } from "../../../features/appMessages/appMessagesSlice";
import { useCreateWalletMutation } from "../../../features/wallet/walletApiSlice";
import { addUserWallet, type CreateWalletDto } from "../../../features/wallet/walletSlice";
import CreateWalletForm from "./CreateWalletForm";
import styles from "./CreateWalletModal.module.scss";

interface CreateWalletModalProps {
    open: boolean;
    setOpen: React.Dispatch<React.SetStateAction<boolean>>;
    setNewWalletId: React.Dispatch<React.SetStateAction<number | null>>;
}

export type CreateWalletFormFields = CreateWalletDto & FormSystemFields;

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
        <Modal open={open} onClose={handleClose}>
            <Fade in={open}>
                <div className={styles.modal_block}>
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
                </div>
            </Fade>
        </Modal>
    );
};

export default CreateWalletModal;
