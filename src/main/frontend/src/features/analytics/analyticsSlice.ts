import { createSlice, type PayloadAction } from "@reduxjs/toolkit";
import dayjs from "dayjs";
import { type RootState } from "../../app/store";
import { clearAuthState } from "../auth/authSlice";
import {
    type Category,
    CategoryType,
    selectCategoriesWithSubcategories,
    selectIsCategoriesInitialized,
} from "../category/categorySlice";
import { getParentCategory } from "../category/categoryUtil";
import { type ViewAnalyticsDto } from "./analyticsApiSlice";

// Record<category, amount>
type CategoryAnalytics = Record<string, number>;

// Record<date, Record<category, amount>>
type DateCategoryAnalytics = Record<string, CategoryAnalytics>;

export interface Analytics {
    income: DateCategoryAnalytics;
    outcome: DateCategoryAnalytics;
}

export enum AnalyticsPeriod {
    DAILY = "DAILY",
    MONTHLY = "MONTHLY",
    YEARLY = "YEARLY"
}

// <CategoryName or "date", value (string for date value and number for category amount)>
export type AnalyticsBarData = Record<string, number | string>;

type AnalyticsState = {
    [walletId in number]: {
        [periodType in AnalyticsPeriod]?: Analytics;
    }
};

const initialState: AnalyticsState = {};

interface SaveDataFromServerPayload {
    walletId: number;
    periodType: AnalyticsPeriod;
    data: ViewAnalyticsDto | undefined;
}

const analyticsSlice = createSlice({
    name: "analytics",
    initialState,
    reducers: {
        saveAnalyticsFromServer(state, action: PayloadAction<SaveDataFromServerPayload>) {
            const { walletId, periodType, data } = action.payload;

            if (data == null) {
                return;
            }
            if (state[walletId] == null) {
                state[walletId] = {};
            }
            state[walletId][periodType] = data;
        },
    },
    extraReducers: builder => builder.addCase(clearAuthState, () => initialState),
});

export const { saveAnalyticsFromServer } = analyticsSlice.actions;

/**
 * Selects analytics data for bar chart (nivo library)
 *
 * @param state - redux state
 * @param walletId - wallet id for which analytics data is selected
 * @param periodType - period type for which analytics data is selected
 * @param date - date for which analytics data is selected
 * @param options - additional options for analytics data (show categories, show child categories)
 *
 * @returns analytics data for bar chart (No more parsing is required)
 */
export const selectAnalyticsForBarChart = (state: RootState,
                                           walletId: number,
                                           periodType: AnalyticsPeriod,
                                           date: string,
                                           options?: {
                                               showCategories: boolean;
                                               showChildCategories: boolean;
                                           },
): AnalyticsBarData[] => {
    const { showCategories, showChildCategories }
        = options ?? { showCategories: false, showChildCategories: false };

    const analyticsData = state.analytics[walletId]?.[periodType];

    const isDisplayCategories: boolean
        = options != null && (options.showCategories || options.showChildCategories);

    if (analyticsData == null
        || (isDisplayCategories && !selectIsCategoriesInitialized(state))) {
        return [];
    }
    const categories = selectCategoriesWithSubcategories(state);

    const dateFrom = getDateFromByPeriodType(date, periodType);
    const dateTo = getDateToByPeriodType(date, periodType);

    const cutIncomeData = cutDataByDate(analyticsData.income, dateFrom, dateTo);
    const cutOutcomeData = cutDataByDate(analyticsData.outcome, dateFrom, dateTo);

    const incomeParsedData = parseAnalyticsDataToBarChartFormat(
        cutIncomeData,
        periodType,
        CategoryType.INCOME,
        categories,
        { showCategories, showChildCategories },
    );
    const outcomeParsedData = parseAnalyticsDataToBarChartFormat(
        cutOutcomeData,
        periodType,
        CategoryType.OUTCOME,
        categories,
        { showCategories, showChildCategories },
    );
    const mergedResult = unionAnalyticsData(incomeParsedData, outcomeParsedData);

    fillWithEmptyDates(mergedResult, dateFrom, dateTo, periodType);
    mergedResult.sort((a, b) => dayjs(a.date).diff(dayjs(b.date)));
    return mergedResult;
};

export default analyticsSlice.reducer;

/**
 * Return date format for a period type (Used in bar chart for grouping)
 *
 * @param periodType - period type
 *
 * @returns format for a period type
 */
const getDateFormatByPeriodType = (periodType: AnalyticsPeriod): string => {
    switch (periodType) {
        case AnalyticsPeriod.DAILY:
            return "DD";
        case AnalyticsPeriod.MONTHLY:
            return "MM";
        case AnalyticsPeriod.YEARLY:
            return "YYYY";
    }
};

/**
 * Return date from which analytics data is selected
 *
 * @param date - date for which analytics data is selected
 * @param periodType - period type for which analytics data is selected
 *
 * @returns date from which analytics data is selected
 */
const getDateFromByPeriodType = (date: string, periodType: AnalyticsPeriod): dayjs.Dayjs => {
    switch (periodType) {
        case AnalyticsPeriod.DAILY:
            return dayjs(date).startOf("month");
        case AnalyticsPeriod.MONTHLY:
            return dayjs(date).startOf("year");
        case AnalyticsPeriod.YEARLY:
            return dayjs(date).subtract(1, "year").startOf("year");
        default:
            return dayjs(date);
    }
};

/**
 * Return date to which analytics data is selected
 *
 * @param date - date for which analytics data is selected
 * @param periodType - period type for which analytics data is selected
 *
 * @returns date to which analytics data is selected
 */
const getDateToByPeriodType = (date: string, periodType: AnalyticsPeriod): dayjs.Dayjs => {
    switch (periodType) {
        case AnalyticsPeriod.DAILY:
            return dayjs(date).endOf("month");
        case AnalyticsPeriod.MONTHLY:
            return dayjs(date).endOf("year");
        case AnalyticsPeriod.YEARLY:
            return dayjs(date).add(1, "year").endOf("year");
        default:
            return dayjs(date);
    }
};

/**
 * Cut analytics data by dateFrom and dateTo
 *
 * @param analytics - analytics data
 * @param dateFrom - date from which analytics data is selected
 * @param dateTo - date to which analytics data is selected
 *
 * @returns analytics data that is between dateFrom and dateTo
 */
const cutDataByDate = (analytics: DateCategoryAnalytics,
                       dateFrom: dayjs.Dayjs,
                       dateTo: dayjs.Dayjs,
): DateCategoryAnalytics => Object.fromEntries(
    Object.entries(analytics).filter(([keyDate]) =>
        !dayjs(keyDate).isBefore(dateFrom) && !dayjs(keyDate).isAfter(dateTo)),
);

/**
 * Parse analytics data to format for bar chart (nivo library)
 *
 * @param analytics - analytics data
 * @param periodType - period type for which analytics data is selected
 * @param categoryType - category type to process
 * @param categories - categories (needed for processing categories if needed)
 * @param options - additional options for analytics data (show categories, show child categories)
 *
 * @returns analytics data for bar chart (Only for one category type)
 */
const parseAnalyticsDataToBarChartFormat = (analytics: DateCategoryAnalytics,
                                            periodType: AnalyticsPeriod,
                                            categoryType: CategoryType,
                                            categories: Category[],
                                            options?: {
                                                showCategories: boolean;
                                                showChildCategories: boolean;
                                            },
): AnalyticsBarData[] => {
    const resultData: AnalyticsBarData[] = [];

    for (const date in analytics) {
        if (!Object.prototype.hasOwnProperty.call(analytics, date)) {
            continue;
        }
        const valueCategoryData = analytics[date];
        if (options?.showCategories != null && options?.showCategories) {
            const parsedData = parseAnalyticsChunkWithCategories(
                valueCategoryData,
                categoryType,
                categories,
                options.showChildCategories,
            );
            resultData.push({
                date: dayjs(date).format(getDateFormatByPeriodType(periodType)),
                ...parsedData,
            });
            continue;
        }
        const parsedData
            = parseAnalyticsChunkWithoutCategories(valueCategoryData, categoryType);
        resultData.push({
            date: dayjs(date).format(getDateFormatByPeriodType(periodType)),
            ...parsedData,
        });
    }
    return resultData;
};

/**
 * Parse analytics chunk with categories or without categories (depends on showChildCategories)
 *
 * @param data - analytics chunk
 * @param categoryType - category type to process
 * @param categories - categories
 * @param showChildCategories - show child categories or group them by parent category
 *
 * @returns analytics chunk for bar chart
 */
const parseAnalyticsChunkWithCategories = (data: CategoryAnalytics,
                                           categoryType: CategoryType,
                                           categories: Category[],
                                           showChildCategories: boolean,
): AnalyticsBarData => {
    const resultData: AnalyticsBarData = {};

    for (const category in data) {
        if (!Object.prototype.hasOwnProperty.call(data, category)) {
            continue;
        }
        const value = data[category];
        const preparedValue = categoryType === CategoryType.INCOME ? value : -value;

        if (showChildCategories) {
            resultData[category] = preparedValue;
            continue;
        }
        const parentCategory = getParentCategory(category, categories);

        if (parentCategory == null) {
            resultData[category] = preparedValue;
            continue;
        }
        const currentValue = resultData[parentCategory.name];

        if (currentValue == null) {
            resultData[parentCategory.name] = preparedValue;
            continue;
        }
        resultData[parentCategory.name] = Number(currentValue) + preparedValue;
    }
    return resultData;
};

/**
 * Parse analytics chunk without categories (group by income or outcome)
 *
 * @param data - analytics chunk
 * @param categoryType - category type to process
 *
 * @returns analytics chunk for bar chart
 */
const parseAnalyticsChunkWithoutCategories = (data: CategoryAnalytics,
                                              categoryType: CategoryType,
): AnalyticsBarData => {
    const resultData: AnalyticsBarData = {};

    for (const category in data) {
        if (!Object.prototype.hasOwnProperty.call(data, category)) {
            continue;
        }
        const value = data[category];
        const preparedValue = categoryType === CategoryType.INCOME ? value : -value;
        const preparedKey = categoryType.toLowerCase();
        const currentValue = resultData[preparedKey];

        if (currentValue == null) {
            resultData[preparedKey] = preparedValue;
            continue;
        }
        resultData[preparedKey] = Number(currentValue) + preparedValue;
    }
    return resultData;
};

/**
 * Union income and outcome analytics data to one array
 *
 * @param incomeData analytics data for income
 * @param outcomeData analytics data for outcome
 *
 * @returns union analytics data
 */
const unionAnalyticsData = (incomeData: AnalyticsBarData[],
                            outcomeData: AnalyticsBarData[],
) => {
    const resultData: AnalyticsBarData[] = [...incomeData];

    for (const outcomeChunk of outcomeData) {
        const incomeChunk = resultData.find(cur => cur.date === outcomeChunk.date);
        if (incomeChunk == null) {
            resultData.push(outcomeChunk);
            continue;
        }
        for (const category in outcomeChunk) {
            if (!Object.prototype.hasOwnProperty.call(outcomeChunk, category)
                || category === "date") {
                continue;
            }
            incomeChunk[category] = outcomeChunk[category];
        }
    }
    return resultData;
};

/**
 * Fill analytics data with empty dates to fill empty spaces in bar chart
 * (add chunks with only date)
 *
 * @param data - analytics data
 * @param dateFrom - date from which fill data
 * @param dateTo - date to which fill data
 * @param periodType - period type for which analytics data is selected (needed for date format)
 */
const fillWithEmptyDates = (data: AnalyticsBarData[],
                            dateFrom: dayjs.Dayjs,
                            dateTo: dayjs.Dayjs,
                            periodType: AnalyticsPeriod,
): void => {
    const resultData = data;

    let currentDate = dateFrom;
    while (currentDate.isBefore(dateTo)) {
        const dateLabel = currentDate.format(getDateFormatByPeriodType(periodType));

        // Is a date not exists in data
        if (resultData.find(el => el.date === dateLabel) == null) {
            resultData.push({ date: dateLabel });
        }
        switch (periodType) {
            case AnalyticsPeriod.DAILY:
                currentDate = currentDate.add(1, "day");
                break;
            case AnalyticsPeriod.MONTHLY:
                currentDate = currentDate.add(1, "month");
                break;
            case AnalyticsPeriod.YEARLY:
                currentDate = currentDate.add(1, "year");
                break;
        }
    }
};
