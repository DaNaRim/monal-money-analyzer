import dayjs from "dayjs";
import utc from "dayjs/plugin/utc";
import { apiSlice } from "../api/apiSlice";

dayjs.extend(utc);

interface ViewAnalyticsParams {
    walletId: number;
    date: string;
}

// Record<date, Record<category, amount>>
export interface ViewAnalyticsDto {
    income: Record<string, Record<string, number>>;
    outcome: Record<string, Record<string, number>>;
}

const analyticsApiSLice = apiSlice.injectEndpoints({
    endpoints: builder => ({
        getDailyAnalytics: builder.query<ViewAnalyticsDto, ViewAnalyticsParams>({
            query: ({ walletId, date }) => ({
                url: "/analytics/daily",
                method: "GET",
                params: {
                    walletId,
                    date: getDailyDateInUtcFormat(date),
                },
            }),
        }),
    }),
});

export const {
    useGetDailyAnalyticsQuery,
} = analyticsApiSLice;

function getDailyDateInUtcFormat(date: string | Date): string {
    return dayjs(date).utc().format("YYYY-MM");
}
