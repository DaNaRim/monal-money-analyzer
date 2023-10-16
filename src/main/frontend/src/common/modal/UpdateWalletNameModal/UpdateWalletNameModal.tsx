import { Fade, Modal } from "@mui/material";
import { type Dispatch, type SetStateAction, useEffect } from "react";
import { useForm } from "react-hook-form";
import useFetchUtils, { type FormSystemFields } from "../../../app/hooks/formUtils";
import { useAppDispatch } from "../../../app/hooks/reduxHooks";
import useTranslation from "../../../app/hooks/translation";
import { useUpdateWalletNameMutation } from "../../../features/wallet/walletApiSlice";
import { updateWallet } from "../../../features/wallet/walletSlice";
import Form from "../../components/form/Form/Form";
import InputText from "../../components/form/InputText/InputText";
import styles from "./UpdateWalletNameModal.module.scss";

interface UpdateWalletNameModalProps {
    open: boolean;
    setOpen: Dispatch<SetStateAction<boolean>>;
    walletId: number;
    walletName: string;
}

type UpdateWalletNameFormFields = FormSystemFields & {
    name: string;
};

const UpdateWalletNameModal = ({
                                   open,
                                   setOpen,
                                   walletId,
                                   walletName,
                               }: UpdateWalletNameModalProps) => {
    const t = useTranslation();
    const dispatch = useAppDispatch();

    const { handleResponseError } = useFetchUtils();

    const [
        updateWalletName,
        { isLoading: isUpdatingWalletName },
    ] = useUpdateWalletNameMutation();

    const {
        register,
        handleSubmit,
        setValue,
        clearErrors,
        setError,
        formState: { errors },
    } = useForm<UpdateWalletNameFormFields>();

    const handleUpdateWalletName = (data: UpdateWalletNameFormFields) => {
        updateWalletName({
            id: walletId,
            name: data.name,
        }).unwrap()
            .then(res => {
                dispatch(updateWallet(res));
                setOpen(false);
            })
            .catch((error) => handleResponseError(error, setError));
    };

    const handleClose = () => {
        clearErrors();
        setOpen(false);
    };

    useEffect(() => {
        setValue("name", walletName);
    }, [walletName, open]);

    return (
        <Modal open={open} onClose={handleClose}>
            <Fade in={open}>
                <div className={styles.modal_block}>
                    <h2>{t.updateWalletNameModal.title}</h2>
                    <Form onSubmit={handleSubmit(handleUpdateWalletName)}
                          componentName={"updateWalletNameModal"}
                          isSubmitting={isUpdatingWalletName}
                          {...{ register, errors }}>

                        <InputText name={"name"}
                                   options={{ required: true }}
                                   componentName={"updateWalletNameModal"}
                                   {...{ register, errors }}/>

                        <button type="submit">{t.updateWalletNameModal.submit}</button>
                    </Form>
                </div>
            </Fade>
        </Modal>
    );
};

export default UpdateWalletNameModal;
