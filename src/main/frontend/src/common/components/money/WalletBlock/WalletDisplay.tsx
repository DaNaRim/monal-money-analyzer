import React from "react";
import { addSpacesToNumber } from "../../../utils/moneyUtils";
import styles from "./WalletBlock.module.scss";
import { type WalletCompProps } from "./WalletComp";

// WalletDisplay is a wallet that is displayed in Select input
const WalletDisplay = ({ name, balance, currency }: WalletCompProps) => {
    const isInRange = (value: number, min: number, max: number) => value >= min && value < max;

    let preparedBalance = balance;

    if (balance.toString().includes("E")) {
        const arr = balance.toString().split("E");
        preparedBalance = Number(arr[0]) * Math.pow(10, Number(arr[1]));
    }
    /*
     Balance max number = 1 000 000 000
     Based on integer part of balance, display balance in different ways:
     i.dd... -> i.dd...
     ii.dd -> ii.dd
     iii.dd -> iii.dd
     i iii.dd -> i iii.dd
     ii iii.dd -> ii iii
     iii iii.dd -> iii iii
     i iii iii.dd -> i.ii'M'
     ii iii iii.dd -> ii.ii'M'
     iii iii iii.dd -> iii.ii'M'
     */
    let processedBalance: string = preparedBalance.toString(); // (i.dd...)

    if (isInRange(preparedBalance, 10, 10_000)) {
        processedBalance = preparedBalance.toFixed(2);
    } else if (isInRange(preparedBalance, 10_000, 1_000_000)) {
        processedBalance = preparedBalance.toFixed(0);
    } else if (isInRange(preparedBalance, 1_000_000, 1_000_000_000)) {
        const fixed = preparedBalance.toFixed(0);
        const integerPart = fixed.substring(0, fixed.length - 6);
        const decimalPart = fixed.substring(fixed.length - 6, fixed.length - 4);
        processedBalance = `${integerPart}.${decimalPart}M`;
    }

    const balanceToDisplay = addSpacesToNumber(processedBalance);

    const nameMaxLength = 20 - balanceToDisplay.length;
    const nameToDisplay = name.length > nameMaxLength
        ? `${name.substring(0, nameMaxLength)}...`
        : name;

    return (
        <div className={styles.wallet}>
            <p className={styles.wallet_name}>{nameToDisplay}</p>
            <div>
                <p className={styles.wallet_balance}>{balanceToDisplay}</p>
                <p className={styles.wallet_currency}>{currency}</p>
            </div>
        </div>
    );
};

export default WalletDisplay;
