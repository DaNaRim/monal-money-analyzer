import dayjs from "dayjs";

import utc from "dayjs/plugin/utc";
import { apiSlice } from "../api/apiSlice";
import {
    type CreateTransactionDto,
    type UpdateTransactionDto,
    type ViewTransactionDto,
} from "./transactionSlice";

dayjs.extend(utc);

interface SelectTransactionsParams {
    walletId: number;
    date: string; // Date in format 'yyyy-MM-dd'
}

const transactionApiSlice = apiSlice.injectEndpoints({
    endpoints: builder => ({
        getTransactions: builder.query<ViewTransactionDto[], SelectTransactionsParams>({
            query: ({ walletId, date }) => ({
                url: "/transaction/date",
                method: "GET",
                params: {
                    walletId,
                    from: getDateInUtcFormat(date),
                    to: getNextDateInUtcFormat(date),
                },
            }),
        }),
        createTransaction: builder.mutation<ViewTransactionDto, CreateTransactionDto>({
            query: transaction => ({
                url: "/transaction",
                method: "POST",
                body: {
                    ...transaction,
                    date: getDateInUtcFormatWithMinutes(transaction.date),
                },
            }),
        }),
        updateTransaction: builder.mutation<ViewTransactionDto, UpdateTransactionDto>({
            query: transaction => ({
                url: "/transaction",
                method: "PUT",
                body: {
                    ...transaction,
                    date: getDateInUtcFormatWithMinutes(transaction.date),
                },
            }),
        }),
        deleteTransaction: builder.mutation<void, number>({
            query: transactionId => ({
                url: "/transaction",
                method: "DELETE",
                params: {
                    transactionId,
                },
            }),
        }),
    }),
});

export const {
    useGetTransactionsQuery,
    useCreateTransactionMutation,
    useUpdateTransactionMutation,
    useDeleteTransactionMutation,
} = transactionApiSlice;

function getDateInUtcFormat(date: string | Date): string {
    return dayjs(date).utc().format("YYYY-MM-DD HH");
}

function getNextDateInUtcFormat(date: string | Date): string {
    return dayjs(date).utc().add(1, "day").format("YYYY-MM-DD HH");
}

function getDateInUtcFormatWithMinutes(date: string | Date): string {
    return dayjs(date).utc().format("YYYY-MM-DD HH:mm");
}
