import React from "react";
import useLocalStorage from "react-use-localstorage";
import WalletBlock from "../../components/money/WalletBlock/WalletBlock";

const TransactionsPage = () => {
    const [selectedWalletId, setSelectedWalletId] = useLocalStorage("selectedWalletId", undefined);

    return (
        <main data-testid="transaction-page">
            <WalletBlock selectedWalletId={selectedWalletId}
                         setSelectedWalletId={setSelectedWalletId}/>
        </main>
    );
};

export default TransactionsPage;
