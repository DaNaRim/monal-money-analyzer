import { Box, Fade, Modal } from "@mui/material";
import { type Dispatch, type SetStateAction, useState } from "react";
import { useAppDispatch } from "../../../app/hooks/reduxHooks";
import useTranslation from "../../../app/hooks/translation";
import {
    useDeleteWalletMutation,
    useGetCountWalletTransactionsQuery,
} from "../../../features/wallet/walletApiSlice";
import { deleteWallet } from "../../../features/wallet/walletSlice";
import styles from "./DeleteWalletModal.module.scss";

interface DeleteWalletModalProps {
    open: boolean;
    setOpen: Dispatch<SetStateAction<boolean>>;
    walletId: number;
}

const DeleteWalletModal = ({ open, setOpen, walletId }: DeleteWalletModalProps) => {
    const t = useTranslation();
    const dispatch = useAppDispatch();

    const [errorMessage, setErrorMessage] = useState<string | null>(null);

    const {
        data: countWalletTransactions,
        isLoading: isLoadingCountWalletTransactions,
        isError: isErrorCountWalletTransactions,
        isSuccess: isSuccessCountWalletTransactions,
    } = useGetCountWalletTransactionsQuery(walletId);

    const [
        deleteWalletMutation, {
            isLoading: isLoadingDeleteWallet,
        },
    ] = useDeleteWalletMutation();

    const handleDelete = () => {
        deleteWalletMutation(walletId).unwrap()
            .then(() => {
                dispatch(deleteWallet(walletId));
                setOpen(false);
            })
            .catch(e => {
                if (e.status === "FETCH_ERROR") {
                    setErrorMessage(t.fetchErrors.fetchError);
                } else {
                    setErrorMessage(e.data[0].message);
                }
            });
    };

    const handleClose = () => {
        setOpen(false);
    };

    return (
        <Modal open={open} onClose={() => setOpen(false)}>
            <Fade in={open}>
                <Box className={styles.modal_block}>
                    <h2 className={styles.modal_title}>{t.deleteWalletModal.title}</h2>
                    {isLoadingCountWalletTransactions && <p>{t.deleteWalletModal.checking}</p>}
                    {isErrorCountWalletTransactions && <>
                      <p className={styles.error}>{t.deleteWalletModal.checkingError}</p>
                      <button className={`${styles.control_button} ${styles.cancel_button}`}
                              onClick={handleClose}>
                          {t.deleteWalletModal.buttons.goBack}
                      </button>
                    </>
                    }
                    {errorMessage != null && <>
                      <p className={styles.error}>{errorMessage}</p>
                      <button className={`${styles.control_button} ${styles.cancel_button}`}
                              onClick={handleClose}>
                          {t.deleteWalletModal.buttons.goBack}
                      </button>
                    </>}
                    {isLoadingDeleteWallet && <p>{t.deleteWalletModal.loading}</p>}
                    {isSuccessCountWalletTransactions
                        && errorMessage == null
                        && !isLoadingDeleteWallet &&
                      <>
                          {countWalletTransactions === 0 &&
                            <>
                              <p className={styles.message}>{t.deleteWalletModal.canDelete}</p>
                              <div className={styles.control_buttons}>
                                <button
                                  className={`${styles.control_button} ${styles.cancel_button}`}
                                  onClick={handleClose}>
                                    {t.deleteWalletModal.buttons.cancel}
                                </button>
                                <button className={styles.control_button} onClick={handleDelete}>
                                    {t.deleteWalletModal.buttons.confirm}
                                </button>
                              </div>
                            </>
                          }
                          {countWalletTransactions > 0 &&
                            <>
                              <p className={styles.message}>
                                  {t.formatString(
                                      t.deleteWalletModal.cannotDelete,
                                      countWalletTransactions,
                                  )}
                              </p>
                              <button className={`${styles.control_button} ${styles.cancel_button}`}
                                      onClick={handleClose}>
                                  {t.deleteWalletModal.buttons.goBack}
                              </button>
                            </>
                          }
                      </>
                    }
                </Box>
            </Fade>
        </Modal>
    );
};

export default DeleteWalletModal;
