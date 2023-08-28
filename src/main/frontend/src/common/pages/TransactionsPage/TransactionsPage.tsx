import React, { useEffect, useState } from "react";
import useLocalStorage from "react-use-localstorage";
import { useAppDispatch, useAppSelector } from "../../../app/hooks/reduxHooks";
import useTranslation from "../../../app/hooks/translation";
import { useGetCategoriesQuery } from "../../../features/category/categoryApiSlice";
import {
    selectIsCategoriesInitialized,
    setCategories,
} from "../../../features/category/categorySlice";
import { useGetUserWalletsQuery } from "../../../features/wallet/walletApiSlice";
import { selectIsWalletsInitialized, setUserWallets } from "../../../features/wallet/walletSlice";
import DateBlock, { getParsedCurrentDate } from "../../components/money/DateBlock/DateBlock";
import TransactionBlock from "../../components/money/TransactionBlock/TransactionBlock";
import WalletBlock from "../../components/money/WalletBlock/WalletBlock";
import CreateTransactionModal from "../../modal/CreateTransactionModal/CreateTransactionModal";
import styles from "./TransactionsPage.module.scss";

const TransactionsPage = () => {
    const t = useTranslation();
    const dispatch = useAppDispatch();

    const [selectedWalletId, setSelectedWalletId] = useLocalStorage("selectedWalletId");

    const [date, setDate] = useState<string>(getParsedCurrentDate());

    const [newWalletModalOpen, setNewWalletModalOpen] = useState<boolean>(false);

    const isWalletsInitialized = useAppSelector<boolean>(selectIsWalletsInitialized);

    const isCategoriesInitialized = useAppSelector<boolean>(selectIsCategoriesInitialized);

    const {
        data: walletsData,
        isLoading: isWalletsLoading,
    } = useGetUserWalletsQuery(undefined, { skip: isWalletsInitialized });

    const { data: fetchedCategories, isLoading: isCategoriesLoading } = useGetCategoriesQuery();

    // Initialize wallets
    useEffect(() => {
        if (!isWalletsInitialized && !isWalletsLoading) {
            dispatch(setUserWallets(walletsData));
        }
    }, [dispatch, isWalletsInitialized, isWalletsLoading, walletsData]);

    // Initialize categories
    useEffect(() => {
        if (!isCategoriesInitialized && !isCategoriesLoading && fetchedCategories != null) {
            dispatch(setCategories(fetchedCategories));
        }
    }, [dispatch, isCategoriesInitialized, isCategoriesLoading, fetchedCategories]);

    return (
        <main className={styles.transaction_page} data-testid="transaction-page">
            <header className={styles.wallet_header}>
                <WalletBlock selectedWalletId={selectedWalletId}
                             setSelectedWalletId={setSelectedWalletId}/>
                <button className={styles.add_transaction_button}
                        onClick={() => setNewWalletModalOpen(true)}>
                    {t.transactionsPage.addNewTransaction}
                </button>
            </header>
            <div className={styles.transaction_left}>
                <DateBlock {...{ date, setDate }}/>
                <TransactionBlock walletId={Number(selectedWalletId)} date={date}/>
            </div>
            <CreateTransactionModal open={newWalletModalOpen}
                                    setOpen={setNewWalletModalOpen}
                                    walletId={Number(selectedWalletId)}/>
        </main>
    );
};

export default TransactionsPage;
