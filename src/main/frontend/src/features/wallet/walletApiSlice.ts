import { apiSlice } from "../api/apiSlice";
import { type CreateWalletDto, type Wallet } from "./walletSlice";

export const walletApiSlice = apiSlice.injectEndpoints({
    endpoints: builder => ({
        getUserWallets: builder.query<Wallet[], void>({
            query: () => ({
                url: "/wallet",
                method: "GET",
            }),
        }),
        createWallet: builder.mutation<Wallet, CreateWalletDto>({
            query: (body) => ({
                url: "/wallet",
                method: "POST",
                body,
            }),
        }),
    }),
});

export const {
    useGetUserWalletsQuery,
    useCreateWalletMutation,
} = walletApiSlice;
