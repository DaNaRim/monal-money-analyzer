import { MenuItem, Select } from "@mui/material";
import React, { useEffect, useState } from "react";
import { useAppSelector } from "../../../../app/hooks/reduxHooks";
import useTranslation from "../../../../app/hooks/translation";
import {
    selectIsWalletsInitialized,
    selectWallets,
    type Wallet,
} from "../../../../features/wallet/walletSlice";
import CreateWalletModal from "../../../modal/CreateWalletModal/CreateWalletModal";
import CreateWalletButton from "./CreateWalletButton";
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

    const [newWalletModalOpen, setNewWalletModalOpen] = useState<boolean>(false);

    const getWalletById = (walletId: number) => wallets.find(wallet => wallet.id === walletId);

    const updateSelectedWallet = (walletId: number) => {
        const wallet = wallets.find(wallet => wallet.id === walletId);

        if (wallet != null) {
            setSelectedWalletId(wallet.id.toString());
        }
    };

    const handleSelectButtonKeyDown = (e: React.KeyboardEvent<HTMLLIElement>) => {
        if (e.key === "Enter") {
            setNewWalletModalOpen(true);
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
                && <CreateWalletButton setNewWalletModalOpen={setNewWalletModalOpen}/>
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
                           data-testid="select-wallet"
              >
                    {/* KeyDown on div because mui ignore focus and keydown events for it's
                     children */}
                <MenuItem value={selectedWalletId}
                          className={styles.add_wallet_button_wrapper}
                          onClick={() => setNewWalletModalOpen(true)}
                          onKeyDown={handleSelectButtonKeyDown}
                >
                  <CreateWalletButton setNewWalletModalOpen={setNewWalletModalOpen}/>
                </MenuItem>
                    {wallets.map(wallet => (
                        <MenuItem value={wallet.id} key={wallet.id}>
                            <WalletComp {...wallet}/>
                        </MenuItem>
                    ))}
              </Select>
            }
            <CreateWalletModal open={newWalletModalOpen}
                               setOpen={setNewWalletModalOpen}
                               setNewWalletId={setNewWalletId}
            />
        </div>
    );
};

export default WalletBlock;
