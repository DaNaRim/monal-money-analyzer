import React, { type Dispatch, type SetStateAction } from "react";
import useTranslation from "../../../../app/hooks/translation";
import styles from "./WalletBlock.module.scss";

interface ChangeWalletBalanceButtonProps {
    setChangeWalletBalanceModalOpen: Dispatch<SetStateAction<boolean>>;
}

const ChangeWalletBalanceButton = ({
                                       setChangeWalletBalanceModalOpen,
                                   }: ChangeWalletBalanceButtonProps) => {
    const t = useTranslation();
    return (
        <button className={styles.action_button}
                onClick={() => setChangeWalletBalanceModalOpen(true)}>
            <div className={styles.text}>{t.walletBlock.changeWalletBalance}</div>
        </button>
    );
};

export default ChangeWalletBalanceButton;
