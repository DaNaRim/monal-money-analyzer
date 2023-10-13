import { MenuItem } from "@mui/material";
import React, { useState } from "react";
import useTranslation from "../../../../app/hooks/translation";
import DeleteWalletModal from "../../../modal/DeleteWalletModal/DeleteWalletModal";
import styles from "./WalletBlock.module.scss";

interface DeleteWalletButtonProps {
    selectedWalletId: number | null;
}

const DeleteWalletButton = ({ selectedWalletId }: DeleteWalletButtonProps) => {
    const t = useTranslation();

    const [deleteWalletModalOpen, setDeleteWalletModalOpen] = useState<boolean>(false);

    const handleButtonKeyDownAction = (e: React.KeyboardEvent<HTMLLIElement>) => {
        if (e.key === "Enter") {
            setDeleteWalletModalOpen(true);
        }
    };

    if (selectedWalletId == null) {
        return null;
    }
    return (
        <>
            <MenuItem value={selectedWalletId}
                      className={styles.action_button_wrapper}
                      onClick={() => setDeleteWalletModalOpen(true)}
                      onKeyDown={handleButtonKeyDownAction}
            >
                <button className={styles.action_button}
                        onClick={() => setDeleteWalletModalOpen(true)}>
                    <div className={styles.text}>{t.walletBlock.deleteWallet}</div>
                </button>
            </MenuItem>
            <DeleteWalletModal open={deleteWalletModalOpen}
                               setOpen={setDeleteWalletModalOpen}
                               walletId={selectedWalletId}/>
        </>
    );
};

export default DeleteWalletButton;
