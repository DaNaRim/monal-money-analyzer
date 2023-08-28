import { describe } from "@jest/globals";
import { CategoryType } from "../../../features/category/categorySlice";
import reducer, {
    addUserWallet,
    setUserWallets,
    updateWalletBalance,
    type Wallet,
    type WalletsState,
} from "../../../features/wallet/walletSlice";

describe("walletSlice", () => {
    test("init state", () => {
        expect(reducer(undefined, { type: undefined })).toEqual({
            wallets: [],
            isInitialized: false,
        });
    });

    test("setUserWallets", () => {
        const wallets: Wallet[] = [
            {
                id: 1,
                name: "Wallet 1",
                balance: 100,
                currency: "USD",
            },
            {
                id: 2,
                name: "Wallet 2",
                balance: 200,
                currency: "USD",
            },
        ];

        expect(reducer(undefined, setUserWallets(wallets))).toEqual({
            wallets,
            isInitialized: true,
        });
    });

    test("setUserWallets. undefined. should be empty array", () => {
        expect(reducer(undefined, setUserWallets(undefined))).toEqual({
            wallets: [],
            isInitialized: true,
        });
    });

    test("addUserWallet", () => {
        const wallets: Wallet[] = [
            {
                id: 1,
                name: "Wallet 1",
                balance: 100,
                currency: "USD",
            },
            {
                id: 2,
                name: "Wallet 2",
                balance: 200,
                currency: "USD",
            },
        ];
        const prevState: WalletsState = {
            wallets,
            isInitialized: true,
        };
        const newWallet: Wallet = {
            id: 3,
            name: "Wallet 3",
            balance: 300,
            currency: "USD",
        };
        expect(reducer(prevState, addUserWallet(newWallet))).toEqual({
            wallets: [...wallets, newWallet],
            isInitialized: true,
        });
    });

    test("updateWalletBalance income", () => {
        const prevState: WalletsState = {
            wallets: [{
                id: 1,
                name: "Wallet 1",
                balance: 100,
                currency: "USD",
            }],
            isInitialized: true,
        };

        expect(reducer(prevState, updateWalletBalance({
            walletId: 1,
            deltaBalance: 10,
            categoryType: CategoryType.INCOME,
        }))?.wallets[0].balance).toEqual(110);
    });

    test("updateWalletBalance outcome", () => {
        const prevState: WalletsState = {
            wallets: [{
                id: 1,
                name: "Wallet 1",
                balance: 100,
                currency: "USD",
            }],
            isInitialized: true,
        };

        expect(reducer(prevState, updateWalletBalance({
            walletId: 1,
            deltaBalance: 10,
            categoryType: CategoryType.OUTCOME,
        }))?.wallets[0].balance).toEqual(90);
    });
});
