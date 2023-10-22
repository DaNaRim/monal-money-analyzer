import useLocalStorage from "react-use-localstorage";
import usePageTitle from "../../../app/hooks/usePageTitle";
import AnalyticsBar from "../../components/money/AnalyticsBar/AnalyticsBar";
import WalletBlock from "../../components/money/WalletBlock/WalletBlock";
import WalletCategoriesInitializer
    from "../../components/money/WalletCategoriesInitializer/WalletCategoriesInitializer";
import { LOCAL_STORAGE_SELECTED_WALLET_ID } from "../../utils/moneyUtils";
import styles from "../TransactionsPage/TransactionsPage.module.scss";

const AnalyticsPage = () => {
    usePageTitle("analyticsPage");

    const [selectedWalletId, setSelectedWalletId]
        = useLocalStorage(LOCAL_STORAGE_SELECTED_WALLET_ID);

    return (
        <WalletCategoriesInitializer>
            <main data-testid="analytics-page">
                <header className={styles.wallet_header}>
                    <WalletBlock selectedWalletId={selectedWalletId}
                                 setSelectedWalletId={setSelectedWalletId}/>
                </header>
                <AnalyticsBar walletId={Number(selectedWalletId)}/>
            </main>
        </WalletCategoriesInitializer>
    );
};

export default AnalyticsPage;
