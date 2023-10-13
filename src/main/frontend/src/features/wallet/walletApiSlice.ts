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
        getCountWalletTransactions: builder.query<number, number>({
            query: (id) => ({
                url: "/wallet/countTransactions",
                method: "GET",
                params: {
                    walletId: id,
                },
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
        deleteWallet: builder.mutation<void, number>({
            query: (id) => ({
                url: "/wallet",
                method: "DELETE",
                params: {
                    walletId: id,
                },
            }),
        }),
    }),
});

export const {
    useGetUserWalletsQuery,
    useGetCountWalletTransactionsQuery,
    useCreateWalletMutation,
    useUpdateWalletNameMutation,
    useDeleteWalletMutation,
} = walletApiSlice;
