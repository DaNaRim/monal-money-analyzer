import dayjs from "dayjs";
import { apiSlice } from "../api/apiSlice";
import { type Transaction } from "./transactionSlice";

interface SelectTransactionsParams {
    walletId: number;
    date: string; // Date in format 'yyyy/MM/dd'
}

const transactionApiSlice = apiSlice.injectEndpoints({
    endpoints: builder => ({
        getTransactions: builder.query<Transaction[], SelectTransactionsParams>({
            query: ({ walletId, date }) => ({
                url: "/transactions",
                method: "GET",
                params: {
                    walletId,
                    from: getDateInUtcFormat(date),
                    to: getNextDateInUtcFormat(date),
                },
            }),
        }),
    }),
});

export const {
    useGetTransactionsQuery,
} = transactionApiSlice;

export default transactionApiSlice.reducer;

// Input date format 'yyyy/MM/dd'
// Output date format 'yyyy-MM-dd HH'
function getDateInUtcFormat(date: string): string {
    const preparedDate = new Date(date);

    const utcDate = new Date(
        preparedDate.getUTCFullYear(),
        preparedDate.getUTCMonth(),
        preparedDate.getUTCDate(),
        preparedDate.getUTCHours(),
    );
    return dayjs(utcDate).format("yyyy-MM-dd HH");
}

// Input date format 'yyyy/MM/dd'
// Output date format 'yyyy-MM-dd HH'
function getNextDateInUtcFormat(date: string): string {
    const preparedDate = new Date(date);

    const utcDate = new Date(
        preparedDate.getUTCFullYear(),
        preparedDate.getUTCMonth(),
        preparedDate.getUTCDate(),
        preparedDate.getUTCHours(),
    );
    return dayjs(utcDate).add(1, "day").format("yyyy-MM-dd HH");
}
