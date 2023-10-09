import { MenuItem } from "@mui/material";
import React, { useState } from "react";
import useTranslation from "../../../../app/hooks/translation";
import { type Wallet } from "../../../../features/wallet/walletSlice";
import ChangeWalletBalanceModal
    from "../../../modal/ChangeWalletBalanceModal/ChangeWalletBalanceModal";
import styles from "./WalletBlock.module.scss";

interface ChangeWalletBalanceButtonProps {
    selectedWallet: Wallet | undefined;
}

/**
 * Button in mui select to change wallet balance. Opens ChangeWalletBalanceModal.
 * Don't display if no wallet is selected.
 *
 * @param selectedWallet - selected wallet
 */
const ChangeWalletBalanceButton = ({ selectedWallet }: ChangeWalletBalanceButtonProps) => {
    const t = useTranslation();

    const [changeWalletBalanceModalOpen, setChangeWalletBalanceModalOpen]
        = useState<boolean>(false);

    const handleButtonKeyDownAction = (e: React.KeyboardEvent<HTMLLIElement>) => {
        if (e.key === "Enter") {
            setChangeWalletBalanceModalOpen(true);
        }
    };

    if (selectedWallet == null) {
        return null;
    }
    return (
        <>
            <MenuItem value={selectedWallet.id}
                      className={styles.action_button_wrapper}
                      onClick={() => setChangeWalletBalanceModalOpen(true)}
                      onKeyDown={handleButtonKeyDownAction}
            >
                <button className={styles.action_button}
                        onClick={() => setChangeWalletBalanceModalOpen(true)}>
                    <div className={styles.text}>{t.walletBlock.changeWalletBalance}</div>
                </button>
            </MenuItem>
            <ChangeWalletBalanceModal open={changeWalletBalanceModalOpen}
                                      setOpen={setChangeWalletBalanceModalOpen}
                                      wallet={selectedWallet}
            />
        </>
    );
};

export default ChangeWalletBalanceButton;
