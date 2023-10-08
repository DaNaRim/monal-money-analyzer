import React from "react";
import useTranslation from "../../../../app/hooks/translation";
import styles from "./WalletBlock.module.scss";

interface CreateWalletButtonProps {
    setNewWalletModalOpen: React.Dispatch<React.SetStateAction<boolean>>;
}

const CreateWalletButton = ({ setNewWalletModalOpen }: CreateWalletButtonProps) => {
    const t = useTranslation();
    return (
        <button className={styles.action_button}
                onClick={() => setNewWalletModalOpen(true)}>
            <div className={styles.plus}>+</div>
            <div className={styles.text}>{t.walletBlock.addNewWallet}</div>
        </button>
    );
};

export default CreateWalletButton;
