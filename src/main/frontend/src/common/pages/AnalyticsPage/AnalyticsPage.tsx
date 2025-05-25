import usePageTitle from "../../../app/hooks/usePageTitle";
import AnalyticsBar from "../../components/money/AnalyticsBar/AnalyticsBar";
import WalletCategoriesInitializer
    from "../../components/money/WalletCategoriesInitializer/WalletCategoriesInitializer";

const AnalyticsPage = () => {
    usePageTitle("analyticsPage");

    // const [selectedWalletId, setSelectedWalletId]
    //     = useLocalStorage(LOCAL_STORAGE_SELECTED_WALLET_ID);

    return (
        <WalletCategoriesInitializer>
            <main data-testid="analytics-page">
                {/*<header className={styles.wallet_header}>*/}
                {/*    <WalletBlock selectedWalletId={selectedWalletId}*/}
                {/*                 setSelectedWalletId={setSelectedWalletId}/>*/}
                {/*</header>*/}
                {/*<AnalyticsBar walletId={Number(selectedWalletId)}/>*/}
                <AnalyticsBar />
            </main>
        </WalletCategoriesInitializer>
    );
};

export default AnalyticsPage;
