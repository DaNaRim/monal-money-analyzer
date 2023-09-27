import dayjs from "dayjs";
import utc from "dayjs/plugin/utc";
import { apiSlice } from "../api/apiSlice";
import { AnalyticsPeriod } from "./analyticsSlice";

dayjs.extend(utc);

interface ViewAnalyticsParams {
    walletId: number;
    date: string;
    period: AnalyticsPeriod;
}

// Record<date, Record<category, amount>>
export interface ViewAnalyticsDto {
    income: Record<string, Record<string, number>>;
    outcome: Record<string, Record<string, number>>;
}

const analyticsApiSLice = apiSlice.injectEndpoints({
    endpoints: builder => ({
        getAnalytics: builder.query<ViewAnalyticsDto, ViewAnalyticsParams>({
            query: ({ walletId, date, period }) => ({
                url: "/analytics",
                method: "GET",
                params: {
                    walletId,
                    date: dayjs(date).utc().format("YYYY-MM"),
                    period
                },
            }),
        }),
    }),
});

export const {
    useGetAnalyticsQuery,
} = analyticsApiSLice;
