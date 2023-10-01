import { Box, Fade, Modal } from "@mui/material";
import { type Dispatch, type SetStateAction, useEffect, useState } from "react";
import { useAppDispatch } from "../../../app/hooks/reduxHooks";
import useTranslation from "../../../app/hooks/translation";
import { useDeleteTransactionMutation } from "../../../features/transaction/transactionApiSlice";
import { deleteTransaction } from "../../../features/transaction/transactionSlice";
import styles from "./DeleteTransactionModal.module.scss";

interface DeleteTransactionModalProps {
    open: boolean;
    setOpen: Dispatch<SetStateAction<boolean>>;
    transactionId: number;
}

/**
 * Modal for confirming deletion of a transaction
 *
 * @param open Whether the modal is open
 * @param setOpen Function to set whether the modal is open
 * @param transactionId ID of the transaction to delete
 */
const DeleteTransactionModal = ({ open, setOpen, transactionId }: DeleteTransactionModalProps) => {
    const t = useTranslation();
    const dispatch = useAppDispatch();

    const [errorMessage, setErrorMessage] = useState<string | null>(null);

    const [
        deleteTransactionMutation, {
            isLoading,
            isError,
            reset: resetMutation,
        },
    ] = useDeleteTransactionMutation();

    const handleDelete = () => {
        deleteTransactionMutation(transactionId).unwrap()
            .then(() => {
                dispatch(deleteTransaction(transactionId));
                setOpen(false);
            })
            .catch(e => {
                if (e.status === "FETCH_ERROR") {
                    setErrorMessage(t.deleteTransactionModal.fetchError);
                } else {
                    setErrorMessage(e.data[0].message);
                }
            });
    };

    // Close modal after 2 seconds if there is an error
    useEffect(() => {
        if (errorMessage !== null) {
            setTimeout(() => setOpen(false), 2_000);
        }
    }, [errorMessage]);

    // Reset error message after 2.5 seconds (0.5 seconds after closing modal)
    useEffect(() => {
        if (errorMessage !== null) {
            setTimeout(() => {
                resetMutation();
                setErrorMessage(null);
            }, 2_500);
        }
    }, [errorMessage]);

    return (
        <Modal open={open} onClose={() => setOpen(false)}>
            <Fade in={open}>
                <Box className={styles.modal_block}>
                    {isLoading && <p>{t.deleteTransactionModal.loading}</p>}
                    {isError && <p className={styles.error}>{errorMessage}</p>}
                    {!isLoading && !isError &&
                      <>
                        <h2 className={styles.modal_title}>{t.deleteTransactionModal.title}</h2>

                        <div className={styles.control_buttons}>
                          <button onClick={() => setOpen(false)}>
                              {t.deleteTransactionModal.buttons.cancel}
                          </button>
                          <button onClick={handleDelete}>
                              {t.deleteTransactionModal.buttons.confirm}
                          </button>
                        </div>
                      </>
                    }
                </Box>
            </Fade>
        </Modal>
    );
};

export default DeleteTransactionModal;
