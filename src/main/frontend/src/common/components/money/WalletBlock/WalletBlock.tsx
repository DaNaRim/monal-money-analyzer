import { MenuItem, Select } from "@mui/material";
import React, { useEffect, useState } from "react";
import { useAppSelector } from "../../../../app/hooks/reduxHooks";
import useTranslation from "../../../../app/hooks/translation";
import {
    selectIsWalletsInitialized,
    selectWallets,
    type Wallet,
} from "../../../../features/wallet/walletSlice";
import ChangeWalletBalanceButton from "./ChangeWalletBalanceButton";
import CreateWalletButton from "./CreateWalletButton";
import DeleteWalletButton from "./DeleteWalletButton";
import UpdateWalletNameButton from "./UpdateWalletNameButton";
import styles from "./WalletBlock.module.scss";
import WalletComp from "./WalletComp";
import WalletDisplay from "./WalletDisplay";

interface WalletBlockProps {
    selectedWalletId: string | undefined;
    setSelectedWalletId: React.Dispatch<string>;
}

const WalletBlock = ({ selectedWalletId, setSelectedWalletId }: WalletBlockProps) => {
    const t = useTranslation();

    const wallets = useAppSelector<Wallet[]>(selectWallets);

    const isWalletsInitialized = useAppSelector<boolean>(selectIsWalletsInitialized);

    const [newWalletId, setNewWalletId] = useState<number | null>(null);

    const getWalletById = (walletId: number) => wallets.find(wallet => wallet.id === walletId);

    const updateSelectedWallet = (walletId: number) => {
        const wallet = wallets.find(wallet => wallet.id === walletId);

        if (wallet != null) {
            setSelectedWalletId(wallet.id.toString());
        }
    };

    // If the default wallet does not exist, select first wallet
    useEffect(() => {
        const isSavedWalletExist
            = wallets.some(wallet => wallet.id.toString() === selectedWalletId);

        if ((selectedWalletId == null || !isSavedWalletExist) && wallets.length > 0) {
            setSelectedWalletId(wallets[0].id.toString());
        }
    }, [wallets, selectedWalletId]);

    // If a new wallet was created, select it
    useEffect(() => {
        if (newWalletId != null) {
            updateSelectedWallet(newWalletId);
            setNewWalletId(null);
        }
    }, [newWalletId, wallets]);

    return (
        <div className={styles.wallet_header} data-testid="wallet-block">
            {!isWalletsInitialized && <div>{t.walletBlock.loading}</div>}
            {isWalletsInitialized && wallets.length === 0
                && <CreateWalletButton selectedWallet={getWalletById(Number(selectedWalletId))}
                                       setNewWalletId={setNewWalletId}/>
            }
            {isWalletsInitialized && wallets.length !== 0
                && <Select className={styles.wallet_select}
                           value={selectedWalletId == null || selectedWalletId === ""
                               ? ""
                               : selectedWalletId}
                           renderValue={id => {
                               const walletToRender = getWalletById(Number(id));
                               if (walletToRender == null) {
                                   // Appears when something went wrong and Select is empty
                                   // Should never happen
                                   return t.walletBlock.noWalletSelected;
                               }
                               return <WalletDisplay {...walletToRender} />;
                           }}
                           onChange={e => updateSelectedWallet(Number(e.target.value))}
                           autoWidth={true}
                           displayEmpty={true} // To display placeholder if smth went wrong
                           label=""
                           MenuProps={{
                               className: styles.wallet_select_menu,
                           }}
                           data-testid="select-wallet"
              >
                <UpdateWalletNameButton selectedWallet={getWalletById(Number(selectedWalletId))}/>
                <ChangeWalletBalanceButton
                  selectedWallet={getWalletById(Number(selectedWalletId))}
                />
                <DeleteWalletButton selectedWalletId={Number(selectedWalletId)}/>
                <CreateWalletButton selectedWallet={getWalletById(Number(selectedWalletId))}
                                    setNewWalletId={setNewWalletId}
                />
                    {wallets.map(wallet => (
                        <MenuItem className={styles.wallet_wrapper}
                                  value={wallet.id}
                                  key={wallet.id}>
                            <WalletComp {...wallet}/>
                        </MenuItem>
                    ))}
              </Select>
            }
        </div>
    );
};

export default WalletBlock;
