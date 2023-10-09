import { MenuItem } from "@mui/material";
import React, { useState } from "react";
import useTranslation from "../../../../app/hooks/translation";
import { type Wallet } from "../../../../features/wallet/walletSlice";
import UpdateWalletNameModal from "../../../modal/UpdateWalletNameModal/UpdateWalletNameModal";
import styles from "./WalletBlock.module.scss";

interface UpdateWalletNameButtonProps {
    selectedWallet: Wallet | undefined;
}

/**
 * Button in mui select to update wallet name. Opens UpdateWalletNameModal.
 * Don't display if no wallet is selected.
 *
 * @param selectedWallet - selected wallet
 */
const UpdateWalletNameButton = ({ selectedWallet }: UpdateWalletNameButtonProps) => {
    const t = useTranslation();

    const [updateWalletNameModalOpen, setUpdateWalletNameModalOpen] = useState<boolean>(false);

    const handleButtonKeyDownAction = (e: React.KeyboardEvent<HTMLLIElement>) => {
        if (e.key === "Enter") {
            setUpdateWalletNameModalOpen(true);
        }
    };

    if (selectedWallet == null) {
        return null;
    }
    return (
        <>
            <MenuItem value={selectedWallet.id}
                      className={styles.action_button_wrapper}
                      onClick={() => setUpdateWalletNameModalOpen(true)}
                      onKeyDown={handleButtonKeyDownAction}
            >
                <button className={styles.action_button}
                        onClick={() => setUpdateWalletNameModalOpen}>
                    <div className={styles.text}>{t.walletBlock.updateWalletName}</div>
                </button>
            </MenuItem>
            <UpdateWalletNameModal open={updateWalletNameModalOpen}
                                   setOpen={setUpdateWalletNameModalOpen}
                                   walletId={selectedWallet.id}
                                   walletName={selectedWallet.name}
            />
        </>
    );
};

export default UpdateWalletNameButton;
