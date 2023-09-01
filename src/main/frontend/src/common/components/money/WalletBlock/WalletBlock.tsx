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
import { addSpacesToNumber } from "../../../utils/moneyUtils";
import styles from "./WalletBlock.module.scss";

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

interface CreateWalletButtonProps {
    setNewWalletModalOpen: React.Dispatch<React.SetStateAction<boolean>>;
}

const CreateWalletButton = ({ setNewWalletModalOpen }: CreateWalletButtonProps) => {
    const t = useTranslation();
    return (
        <button className={styles.add_wallet_button}
                onClick={() => setNewWalletModalOpen(true)}>
            <div className={styles.plus}>+</div>
            <div className={styles.text}>{t.walletBlock.addNewWallet}</div>
        </button>
    );
};

interface WalletCompProps {
    name: string;
    balance: number;
    currency: string;
}

// WalletComp is a wallet that is displayed in a Select dropdown list
const WalletComp = ({ name, balance, currency }: WalletCompProps) => {
    const t = useTranslation();

    return (
        <div className={styles.wallet}>
            <p className={styles.wallet_name}>{name}</p>
            <div>
                <p className={styles.wallet_balance}>{addSpacesToNumber(balance)}</p>
                <p className={styles.wallet_currency}
                   title={t.getString(`data.currency.${currency}`)}>
                    {currency}
                </p>
            </div>
        </div>
    );
};

// WalletDisplay is a wallet that is displayed in Select input
const WalletDisplay = ({ name, balance, currency }: WalletCompProps) => {
    const isInRange = (value: number, min: number, max: number) => value >= min && value < max;

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
    let processedBalance: string = balance.toString(); // (i.dd...)

    if (isInRange(balance, 10, 10_000)) {
        processedBalance = balance.toFixed(2);
    } else if (isInRange(balance, 10_000, 1_000_000)) {
        processedBalance = balance.toFixed(0);
    } else if (isInRange(balance, 1_000_000, 1_000_000_000)) {
        const fixed = balance.toFixed(0);
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
