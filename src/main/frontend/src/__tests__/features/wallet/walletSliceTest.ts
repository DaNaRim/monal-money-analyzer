import { describe } from "@jest/globals";
import reducer, {
    addUserWallet,
    setUserWallets,
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
});
