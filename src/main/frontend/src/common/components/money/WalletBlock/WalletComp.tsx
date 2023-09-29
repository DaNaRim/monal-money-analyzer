import React from "react";
import useTranslation from "../../../../app/hooks/translation";
import { addSpacesToNumber } from "../../../utils/moneyUtils";
import styles from "./WalletBlock.module.scss";

export interface WalletCompProps {
    name: string;
    balance: number;
    currency: string;
}

// WalletComp is a wallet that is displayed in a Select dropdown list
const WalletComp = ({ name, balance, currency }: WalletCompProps) => {
    const t = useTranslation();

    return (
        <div className={styles.wallet}>
            <p className={styles.wallet_name}>{name}</p>
            <div>
                <p className={styles.wallet_balance}>{addSpacesToNumber(balance)}</p>
                <p className={styles.wallet_currency}
                   title={t.getString(`data.currency.${currency}`)}>
                    {currency}
                </p>
            </div>
        </div>
    );
};

export default WalletComp;
