import { createSlice, type PayloadAction } from "@reduxjs/toolkit";
import { type RootState } from "../../app/store";

export const WALLET_BALANCE_MAX_VALUE = 1_000_000_000;
export const WALLET_BALANCE_PRECISION_VALUE = 0.00000001; // 8 digits after dot

export interface CreateWalletDto {
    name: string;
    balance: number;
    currency: string;
}

export interface Wallet {
    id: number;
    name: string;
    balance: number;
    currency: string;
}

interface WalletsState {
    wallets: Wallet[];
    isInitialized: boolean;
}

const initialState: WalletsState = {
    wallets: [],
    isInitialized: false,
};

const walletSlice = createSlice({
    name: "wallets",
    initialState,
    reducers: {
        setUserWallets(state, action: PayloadAction<Wallet[]>) {
            state.wallets = action.payload;
        },
        addUserWallet(state, action: PayloadAction<Wallet>) {
            state.wallets.push(action.payload);
        },
    },
});

export const selectWallets = (state: RootState) => state.wallets.wallets;
export const selectIsWalletsInitialized = (state: RootState) => state.wallets.isInitialized;

export const { setUserWallets, addUserWallet } = walletSlice.actions;

export default walletSlice.reducer;
