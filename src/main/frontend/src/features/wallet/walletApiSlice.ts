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
        updateWalletName: builder.mutation<Wallet, { id: number, name: string }>({
            query: ({ id, name }) => ({
                url: "/wallet/name",
                method: "PUT",
                params: {
                    walletId: id,
                    name,
                },
            }),
        }),
    }),
});

export const {
    useGetUserWalletsQuery,
    useCreateWalletMutation,
    useUpdateWalletNameMutation,
} = walletApiSlice;
