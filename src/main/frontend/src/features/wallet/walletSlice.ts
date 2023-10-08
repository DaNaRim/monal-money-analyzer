import { createSlice, type PayloadAction } from "@reduxjs/toolkit";
import { type RootState } from "../../app/store";
import { clearAuthState } from "../auth/authSlice";
import { CategoryType } from "../category/categorySlice";

export const WALLET_BALANCE_MAX_VALUE = 1_000_000_000;
export const WALLET_BALANCE_PRECISION_VALUE = 0.00000001; // 8 digits after dot

export interface Wallet {
    id: number;
    name: string;
    balance: number;
    currency: string;
}

export interface WalletsState {
    wallets: Wallet[];
    isInitialized: boolean;
}

const initialState: WalletsState = {
    wallets: [],
    isInitialized: false,
};

export interface CreateWalletDto {
    name: string;
    balance: number;
    currency: string;
}

interface UpdateWalletBalancePayload {
    walletId: number;
    deltaBalance: number;
    categoryType: CategoryType;
}

const walletsSlice = createSlice({
    name: "wallets",
    initialState,
    reducers: {
        setUserWallets(state, action: PayloadAction<Wallet[] | undefined>) {
            state.isInitialized = true;
            if (action.payload == null) {
                return;
            }
            state.wallets = action.payload;
        },
        addUserWallet(state, action: PayloadAction<Wallet>) {
            state.wallets.push(action.payload);
        },
        updateWalletBalance(state, action: PayloadAction<UpdateWalletBalancePayload>) {
            const { walletId, deltaBalance, categoryType } = action.payload;

            const wallet = state.wallets.find(wallet => wallet.id === walletId);
            if (wallet == null) {
                return;
            }
            if (categoryType === CategoryType.INCOME) {
                wallet.balance += deltaBalance;
            } else if (categoryType === CategoryType.OUTCOME) {
                wallet.balance -= deltaBalance;
            }
        },
        updateWallet(state, action: PayloadAction<Wallet>) {
            const { id, name, balance, currency } = action.payload;

            const wallet = state.wallets.find(wallet => wallet.id === id);
            if (wallet == null) {
                return;
            }
            wallet.name = name;
            wallet.balance = balance;
            wallet.currency = currency;
        },
    },
    extraReducers: builder => builder.addCase(clearAuthState, () => initialState),
});

export const {
    setUserWallets,
    addUserWallet,
    updateWalletBalance,
    updateWallet,
} = walletsSlice.actions;

export const selectWallets = (state: RootState) => state.wallets.wallets;
export const selectIsWalletsInitialized = (state: RootState) => state.wallets.isInitialized;
export const selectIsWalletsExists = (state: RootState) => state.wallets.wallets.length > 0;

export const selectWalletNameById = (state: RootState, walletId: number) =>
    state.wallets.wallets.find(wallet => wallet.id === walletId)?.name ?? "";

export default walletsSlice.reducer;
