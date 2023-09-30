import dayjs from "dayjs";
import React, { useState } from "react";
import useLocalStorage from "react-use-localstorage";
import { useAppSelector } from "../../../app/hooks/reduxHooks";
import useTranslation from "../../../app/hooks/translation";
import { selectIsWalletsExists } from "../../../features/wallet/walletSlice";
import DailyAnalyticsBlock from "../../components/money/DailyAnalyticsBlock/DailyAnalyticsBlock";
import DateBlock, { DATE_BLOCK_DATE_FORMAT } from "../../components/money/DateBlock/DateBlock";
import TransactionBlock from "../../components/money/TransactionBlock/TransactionBlock";
import WalletBlock from "../../components/money/WalletBlock/WalletBlock";
import WalletCategoriesInitializer
    from "../../components/money/WalletCategoriesInitializer/WalletCategoriesInitializer";
import CreateTransactionModal from "../../modal/CreateTransactionModal/CreateTransactionModal";
import { LOCAL_STORAGE_SELECTED_WALLET_ID } from "../../utils/moneyUtils";
import styles from "./TransactionsPage.module.scss";

const TransactionsPage = () => {
    const t = useTranslation();

    const [selectedWalletId, setSelectedWalletId]
        = useLocalStorage(LOCAL_STORAGE_SELECTED_WALLET_ID);

    const [date, setDate] = useState<string>(getParsedCurrentDate());

    const [newWalletModalOpen, setNewWalletModalOpen] = useState<boolean>(false);

    const isWalletsExists = useAppSelector(selectIsWalletsExists);

    return (
        <WalletCategoriesInitializer>
            <main className={styles.transaction_page} data-testid="transaction-page">
                <header className={styles.wallet_header}>
                    <WalletBlock selectedWalletId={selectedWalletId}
                                 setSelectedWalletId={setSelectedWalletId}/>
                    {isWalletsExists && (
                        <button className={styles.add_transaction_button}
                                onClick={() => setNewWalletModalOpen(true)}>
                            {t.transactionsPage.addNewTransaction}
                        </button>
                    )}
                </header>
                <div className={styles.wrapper}>
                    <div className={styles.transaction_left}>
                        <DateBlock {...{ date, setDate }}/>
                        <TransactionBlock walletId={Number(selectedWalletId)} date={date}/>
                    </div>
                    <div className={styles.transaction_right}>
                        <DailyAnalyticsBlock walletId={Number(selectedWalletId)} date={date}/>
                    </div>
                </div>
                <CreateTransactionModal open={newWalletModalOpen}
                                        setOpen={setNewWalletModalOpen}
                                        walletId={Number(selectedWalletId)}
                                        date={date}/>
            </main>
        </WalletCategoriesInitializer>
    );
};

export default TransactionsPage;

// Return the current date in the format YYYY-MM-DD
export function getParsedCurrentDate(): string {
    return dayjs(new Date()).format(DATE_BLOCK_DATE_FORMAT);
}
