import { useEffect } from "react";
import { useAppDispatch, useAppSelector } from "../../../../app/hooks/reduxHooks";
import { useGetCategoriesQuery } from "../../../../features/category/categoryApiSlice";
import {
    selectIsCategoriesInitialized,
    setCategories,
} from "../../../../features/category/categorySlice";
import { useGetUserWalletsQuery } from "../../../../features/wallet/walletApiSlice";
import {
    selectIsWalletsInitialized,
    setUserWallets,
} from "../../../../features/wallet/walletSlice";

interface WalletCategoriesInitializerProps {
    children: JSX.Element;
}

/**
 * This component is used to initialize wallets and categories. Returns provided children.
 * @param children - children to return
 */
const WalletCategoriesInitializer = ({ children }: WalletCategoriesInitializerProps) => {
    const dispatch = useAppDispatch();

    const isWalletsInitialized = useAppSelector(selectIsWalletsInitialized);

    const isCategoriesInitialized = useAppSelector(selectIsCategoriesInitialized);

    const {
        data: walletsData,
        isLoading: isWalletsLoading,
    } = useGetUserWalletsQuery(undefined, { skip: isWalletsInitialized });

    const {
        data: categoriesData,
        isLoading: isCategoriesLoading,
    } = useGetCategoriesQuery(undefined, { skip: isCategoriesInitialized });

    // Initialize wallets
    useEffect(() => {
        if (!isWalletsInitialized && !isWalletsLoading) {
            dispatch(setUserWallets(walletsData));
        }
    }, [walletsData]);

    // Initialize categories
    useEffect(() => {
        if (!isCategoriesInitialized && !isCategoriesLoading && categoriesData != null) {
            dispatch(setCategories(categoriesData));
        }
    }, [categoriesData]);

    return children;
};

export default WalletCategoriesInitializer;
