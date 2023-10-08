import { type Dispatch, type SetStateAction } from "react";
import useTranslation from "../../../../app/hooks/translation";
import styles from "./WalletBlock.module.scss";

interface UpdateWalletNameButtonProps {
    setUpdateWalletNameModalOpen: Dispatch<SetStateAction<boolean>>;
}

const UpdateWalletNameButton = ({ setUpdateWalletNameModalOpen }: UpdateWalletNameButtonProps) => {
    const t = useTranslation();

    return (
        <button className={styles.action_button} onClick={() => setUpdateWalletNameModalOpen}>
            <div className={styles.text}>{t.walletBlock.updateWalletName}</div>
        </button>
    );
};

export default UpdateWalletNameButton;
