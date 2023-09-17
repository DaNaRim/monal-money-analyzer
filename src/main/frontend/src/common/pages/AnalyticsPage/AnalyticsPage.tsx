import React, { useEffect } from "react";
import useLocalStorage from "react-use-localstorage";
import { useAppDispatch, useAppSelector } from "../../../app/hooks/reduxHooks";
import { useGetCategoriesQuery } from "../../../features/category/categoryApiSlice";
import {
    selectIsCategoriesInitialized,
    setCategories,
} from "../../../features/category/categorySlice";
import { useGetUserWalletsQuery } from "../../../features/wallet/walletApiSlice";
import { selectIsWalletsInitialized, setUserWallets } from "../../../features/wallet/walletSlice";
import AnalyticsBar from "../../components/money/AnalyticsBar/AnalyticsBar";
import WalletBlock from "../../components/money/WalletBlock/WalletBlock";
import styles from "../TransactionsPage/TransactionsPage.module.scss";

const AnalyticsPage = () => {
    const dispatch = useAppDispatch();

    const [selectedWalletId, setSelectedWalletId] = useLocalStorage("selectedWalletId");

    const isWalletsInitialized = useAppSelector(selectIsWalletsInitialized);

    const isCategoriesInitialized = useAppSelector(selectIsCategoriesInitialized);

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
        <main data-testid="analytics-page">
            <header className={styles.wallet_header}>
                <WalletBlock selectedWalletId={selectedWalletId}
                             setSelectedWalletId={setSelectedWalletId}/>
            </header>
            <AnalyticsBar walletId={Number(selectedWalletId)}/>
        </main>
    );
};

export default AnalyticsPage;
