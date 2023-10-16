import { MenuItem, Select } from "@mui/material";
import { type Dispatch, type KeyboardEvent, type SetStateAction, useEffect, useState } from "react";
import { useAppSelector } from "../../../../app/hooks/reduxHooks";
import useTranslation from "../../../../app/hooks/translation";
import {
    selectIsWalletsInitialized,
    selectWallets,
    type Wallet,
} from "../../../../features/wallet/walletSlice";
import ChangeWalletBalanceModal
    from "../../../modal/ChangeWalletBalanceModal/ChangeWalletBalanceModal";
import CreateWalletModal from "../../../modal/CreateWalletModal/CreateWalletModal";
import DeleteWalletModal from "../../../modal/DeleteWalletModal/DeleteWalletModal";
import UpdateWalletNameModal from "../../../modal/UpdateWalletNameModal/UpdateWalletNameModal";
import styles from "./WalletBlock.module.scss";
import WalletComp from "./WalletComp";
import WalletDisplay from "./WalletDisplay";

interface WalletBlockProps {
    selectedWalletId: string | undefined;
    setSelectedWalletId: Dispatch<string>;
}

const WalletBlock = ({ selectedWalletId, setSelectedWalletId }: WalletBlockProps) => {
    const t = useTranslation();

    const wallets = useAppSelector<Wallet[]>(selectWallets);
    const isWalletsInitialized = useAppSelector<boolean>(selectIsWalletsInitialized);

    const [updateWalletNameModalOpen, setUpdateWalletNameModalOpen] = useState<boolean>(false);
    const [changeWalletBalanceModalOpen, setChangeWalletBalanceModalOpen]
        = useState<boolean>(false);
    const [deleteWalletModalOpen, setDeleteWalletModalOpen] = useState<boolean>(false);
    const [newWalletModalOpen, setNewWalletModalOpen] = useState<boolean>(false);

    const [newWalletId, setNewWalletId] = useState<number | null>(null);

    const getWalletById = (walletId: number) => wallets.find(wallet => wallet.id === walletId);

    const updateSelectedWallet = (walletId: number) => {
        const wallet = wallets.find(wallet => wallet.id === walletId);

        if (wallet != null) {
            setSelectedWalletId(wallet.id.toString());
        }
    };

    const handleActionButtonKeyDown = (e: KeyboardEvent<HTMLLIElement>,
                                       action: Dispatch<SetStateAction<boolean>>,
    ) => {
        if (e.key === "Enter") {
            action(true);
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
                && <button className={styles.no_wallets_button}
                           onClick={() => setNewWalletModalOpen(true)}>
                <div className={styles.plus}>+</div>
                <div className={styles.text}>{t.walletBlock.addNewWallet}</div>
              </button>
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
                <MenuItem value={selectedWalletId}
                          className={styles.action_button_wrapper}
                          role="button"
                          onClick={() => setUpdateWalletNameModalOpen(true)}
                          onKeyDown={e =>
                              handleActionButtonKeyDown(e, setUpdateWalletNameModalOpen)
                          }
                >
                  <div className={styles.text}>{t.walletBlock.updateWalletName}</div>
                </MenuItem>
                <MenuItem value={selectedWalletId}
                          className={styles.action_button_wrapper}
                          role="button"
                          onClick={() => setChangeWalletBalanceModalOpen(true)}
                          onKeyDown={e =>
                              handleActionButtonKeyDown(e, setChangeWalletBalanceModalOpen)
                          }
                >
                  <div className={styles.text}>{t.walletBlock.changeWalletBalance}</div>
                </MenuItem>
                <MenuItem value={selectedWalletId}
                          className={styles.action_button_wrapper}
                          role="button"
                          onClick={() => setDeleteWalletModalOpen(true)}
                          onKeyDown={e => handleActionButtonKeyDown(e, setDeleteWalletModalOpen)}
                >
                  <div className={styles.text}>{t.walletBlock.deleteWallet}</div>
                </MenuItem>
                <MenuItem value={selectedWalletId}
                          className={styles.action_button_wrapper}
                          role="button"
                          onClick={() => setNewWalletModalOpen(true)}
                          onKeyDown={e => handleActionButtonKeyDown(e, setNewWalletModalOpen)}
                >
                  <div className={styles.plus}>+</div>
                  <div className={styles.text}>{t.walletBlock.addNewWallet}</div>
                </MenuItem>

                    {wallets.map(wallet => (
                        <MenuItem className={styles.wallet_wrapper}
                                  value={wallet.id}
                                  key={wallet.id}>
                            <WalletComp {...wallet}/>
                        </MenuItem>
                    ))}
              </Select>
            }
            <UpdateWalletNameModal open={updateWalletNameModalOpen}
                                   setOpen={setUpdateWalletNameModalOpen}
                                   walletId={Number(selectedWalletId)}
                                   walletName={getWalletById(Number(selectedWalletId))?.name ?? ""}
            />
            <ChangeWalletBalanceModal open={changeWalletBalanceModalOpen}
                                      setOpen={setChangeWalletBalanceModalOpen}
                                      wallet={getWalletById(Number(selectedWalletId)) ?? null}
            />
            <DeleteWalletModal open={deleteWalletModalOpen}
                               setOpen={setDeleteWalletModalOpen}
                               walletId={Number(selectedWalletId)}
            />
            <CreateWalletModal open={newWalletModalOpen}
                               setOpen={setNewWalletModalOpen}
                               setNewWalletId={setNewWalletId}
            />
        </div>
    );
};

export default WalletBlock;
