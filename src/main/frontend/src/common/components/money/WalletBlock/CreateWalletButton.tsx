import { MenuItem } from "@mui/material";
import React, { type Dispatch, type SetStateAction, useState } from "react";
import useTranslation from "../../../../app/hooks/translation";
import { type Wallet } from "../../../../features/wallet/walletSlice";
import CreateWalletModal from "../../../modal/CreateWalletModal/CreateWalletModal";
import styles from "./WalletBlock.module.scss";

interface CreateWalletButtonProps {
    selectedWallet: Wallet | undefined;
    setNewWalletId: Dispatch<SetStateAction<number | null>>;
}

/**
 * Button in mui select to create new wallet. Opens CreateWalletModal.
 * Displays even if no wallet is selected.
 *
 * @param selectedWallet - selected wallet.
 * @param setNewWalletId - function to set new wallet id after creation
 */
const CreateWalletButton = ({ selectedWallet, setNewWalletId }: CreateWalletButtonProps) => {
    const t = useTranslation();

    const [newWalletModalOpen, setNewWalletModalOpen] = useState<boolean>(false);

    const handleButtonKeyDownAction = (e: React.KeyboardEvent<HTMLLIElement>) => {
        if (e.key === "Enter") {
            setNewWalletModalOpen(true);
        }
    };

    return (
        <>
            <MenuItem value={selectedWallet?.id}
                      className={styles.action_button_wrapper}
                      onClick={() => setNewWalletModalOpen(true)}
                      onKeyDown={handleButtonKeyDownAction}
            >
                <button className={styles.action_button}
                        onClick={() => setNewWalletModalOpen(true)}>
                    <div className={styles.plus}>+</div>
                    <div className={styles.text}>{t.walletBlock.addNewWallet}</div>
                </button>
            </MenuItem>
            <CreateWalletModal open={newWalletModalOpen}
                               setOpen={setNewWalletModalOpen}
                               setNewWalletId={setNewWalletId}
            />
        </>
    );
};

export default CreateWalletButton;
