import { describe } from "@jest/globals";
import { setupStore } from "../../../app/store";
import { type ViewAnalyticsDto } from "../../../features/analytics/analyticsApiSlice";
import reducer, {
    AnalyticsPeriod,
    saveAnalyticsFromServer,
    selectAnalyticsForBarChart,
} from "../../../features/analytics/analyticsSlice";
import { clearAuthState } from "../../../features/auth/authSlice";
import { type Category, CategoryType } from "../../../features/category/categorySlice";

const analytics = {
    1: {
        [AnalyticsPeriod.DAILY]: {
            income: {
                "2021-01-01": {
                    "Category 1": 100,
                    "Subcategory 1": 200,
                    "Subcategory 2": 100,
                },
            },
            outcome: {
                "2021-01-01": {
                    "Category 3": 100,
                },
                "2021-01-02": {
                    "Category 3": 100,
                },
                "2021-01-03": {
                    "Subcategory 3": 100,
                },
            },
        },
    },
};

const categories: Category[] = [
    {
        id: 1,
        name: "Category 1",
        type: CategoryType.INCOME,
        subCategories: [
            {
                id: 11,
                name: "Subcategory 1",
                type: CategoryType.INCOME,
                subCategories: null,
            },
            {
                id: 12,
                name: "Subcategory 2",
                type: CategoryType.INCOME,
                subCategories: null,
            },
        ],
    },
    {
        id: 3,
        name: "Category 3",
        type: CategoryType.OUTCOME,
        subCategories: [
            {
                id: 31,
                name: "Subcategory 3",
                type: CategoryType.OUTCOME,
                subCategories: null,
            },
        ],
    },
];

const storeForSelect = setupStore({
    analytics,
    categories: {
        categories,
        isInitialized: true,
    },
});

describe("analyticsSlice", () => {
    test("initialState", () => {
        expect(reducer(undefined, { type: undefined })).toEqual({});
    });

    // saveAnalyticsFromServer

    test("saveAnalyticsFromServer. Daily", () => {
        const analytics: ViewAnalyticsDto = {
            income: {
                "2021-01-01": {
                    "Category 1": 100,
                    "Category 2": 200,
                },
                "2021-01-02": {
                    "Category 1": 100,
                },
            },
            outcome: {
                "2021-01-02": {
                    "Category 1": 100,
                },
            },
        };
        const payload = {
            walletId: 1,
            periodType: AnalyticsPeriod.DAILY,
            data: analytics,
        };
        expect(reducer(undefined, saveAnalyticsFromServer(payload))).toEqual({
            1: {
                [AnalyticsPeriod.DAILY]: analytics,
            },
        });
    });

    test("saveAnalyticsFromServer. Monthly", () => {
        const analytics: ViewAnalyticsDto = {
            income: {
                "2021-01": {
                    "Category 1": 100,
                    "Category 2": 200,
                },
                "2021-02": {
                    "Category 1": 100,
                },
            },
            outcome: {
                "2021-02": {
                    "Category 1": 100,
                },
            },
        };
        const payload = {
            walletId: 1,
            periodType: AnalyticsPeriod.MONTHLY,
            data: analytics,
        };
        expect(reducer(undefined, saveAnalyticsFromServer(payload))).toEqual({
            1: {
                [AnalyticsPeriod.MONTHLY]: analytics,
            },
        });
    });

    test("saveAnalyticsFromServer. Yearly", () => {
        const analytics: ViewAnalyticsDto = {
            income: {
                2021: {
                    "Category 1": 100,
                    "Category 2": 200,
                },
                2022: {
                    "Category 1": 100,
                },
            },
            outcome: {
                2022: {
                    "Category 1": 100,
                },
            },
        };
        const payload = {
            walletId: 1,
            periodType: AnalyticsPeriod.YEARLY,
            data: analytics,
        };
        expect(reducer(undefined, saveAnalyticsFromServer(payload))).toEqual({
            1: {
                [AnalyticsPeriod.YEARLY]: analytics,
            },
        });
    });

    test("saveAnalyticsFromServe. empty date -> set empty", () => {
        const analytics: ViewAnalyticsDto = {
            income: {},
            outcome: {},
        };
        const payload = {
            walletId: 1,
            periodType: AnalyticsPeriod.DAILY,
            data: analytics,
        };
        expect(reducer(undefined, saveAnalyticsFromServer(payload))).toEqual({
            1: {
                [AnalyticsPeriod.DAILY]: {
                    income: {},
                    outcome: {},
                },
            },
        });
    });

    test("saveAnalyticsFromServer. undefined data -> no changes", () => {
        const payload = {
            walletId: 1,
            periodType: AnalyticsPeriod.DAILY,
            data: undefined,
        };
        expect(reducer(undefined, saveAnalyticsFromServer(payload))).toEqual({});
    });

    test("saveAnalyticsFromServer. Data already exist -> replace", () => {
        const prevState = {
            1: {
                [AnalyticsPeriod.DAILY]: {
                    income: {
                        "2021-01-01": {
                            "Category 3": 50,
                        },
                    },
                    outcome: {},
                },
            },
        };
        const analytics: ViewAnalyticsDto = {
            income: {
                "2021-01-01": {
                    "Category 1": 100,
                    "Category 2": 200,
                },
                "2021-01-02": {
                    "Category 1": 100,
                },
            },
            outcome: {},
        };

        const payload = {
            walletId: 1,
            periodType: AnalyticsPeriod.DAILY,
            data: analytics,
        };
        expect(reducer(prevState, saveAnalyticsFromServer(payload))).toEqual({
            1: {
                [AnalyticsPeriod.DAILY]: {
                    income: {
                        "2021-01-01": {
                            "Category 1": 100,
                            "Category 2": 200,
                        },
                        "2021-01-02": {
                            "Category 1": 100,
                        },
                    },
                    outcome: {},
                },
            },
        });
    });

    test("saveAnalyticsFromServer. Data already exist different period -> add", () => {
        const prevState = {
            1: {
                [AnalyticsPeriod.DAILY]: {
                    income: {
                        "2021-01-01": {
                            "Category 3": 50,
                        },
                    },
                    outcome: {},
                },
            },
        };
        const analytics: ViewAnalyticsDto = {
            income: {
                "2021-01": {
                    "Category 1": 100,
                    "Category 2": 200,
                },
                "2021-02": {
                    "Category 1": 100,
                },
            },
            outcome: {},
        };

        const payload = {
            walletId: 1,
            periodType: AnalyticsPeriod.MONTHLY,
            data: analytics,
        };
        expect(reducer(prevState, saveAnalyticsFromServer(payload))).toEqual({
            1: {
                [AnalyticsPeriod.DAILY]: {
                    income: {
                        "2021-01-01": {
                            "Category 3": 50,
                        },
                    },
                    outcome: {},
                },
                [AnalyticsPeriod.MONTHLY]: analytics,
            },
        });
    });

    // Extra reducers

    test("clearAuthState -> reset state", () => {
        const prevState = {
            1: {
                [AnalyticsPeriod.DAILY]: {
                    income: {
                        "2021-01-01": {
                            "Category 3": 50,
                        },
                    },
                    outcome: {},
                },
            },
        };
        expect(reducer(prevState, clearAuthState)).toEqual({});
    });

    // selectAnalyticsForBarChart

    test("selectAnalyticsForBarChart Daily", () => {
        const result = selectAnalyticsForBarChart(
            storeForSelect.getState(),
            1,
            AnalyticsPeriod.DAILY,
            "2021-01-01",
        );
        expect(result).toEqual([
            {
                date: "01",
                income: 400,
                outcome: -100,
            },
            {
                date: "02",
                outcome: -100,
            },
            {
                date: "03",
                outcome: -100,
            },
            ...emptyDates,
        ]);
    });

    test("selectAnalyticsForBarChart Daily showCategories", () => {
        const result = selectAnalyticsForBarChart(
            storeForSelect.getState(),
            1,
            AnalyticsPeriod.DAILY,
            "2021-01-10",
            {
                showCategories: true,
                showChildCategories: false,
            },
        );
        expect(result).toEqual([
            {
                date: "01",
                "Category 1": 400,
                "Category 3": -100,
            },
            {
                date: "02",
                "Category 3": -100,
            },
            {
                date: "03",
                "Category 3": -100,
            },
            ...emptyDates,
        ]);
    });

    test("selectAnalyticsForBarChart Daily showChildCategories", () => {
        const result = selectAnalyticsForBarChart(
            storeForSelect.getState(),
            1,
            AnalyticsPeriod.DAILY,
            "2021-01-10",
            {
                showCategories: true,
                showChildCategories: true,
            },
        );
        expect(result).toEqual([
            {
                date: "01",
                "Category 1": 100,
                "Subcategory 1": 200,
                "Subcategory 2": 100,
                "Category 3": -100,
            },
            {
                date: "02",
                "Category 3": -100,
            },
            {
                date: "03",
                "Subcategory 3": -100,
            },
            ...emptyDates,
        ]);
    });

    test("selectAnalyticsForBarChart Daily showChildCategories & !showCategories -> no categories",
        () => {
            const result = selectAnalyticsForBarChart(
                storeForSelect.getState(),
                1,
                AnalyticsPeriod.DAILY,
                "2021-01-10",
                {
                    showCategories: false,
                    showChildCategories: true,
                },
            );
            expect(result).toEqual([
                {
                    date: "01",
                    income: 400,
                    outcome: -100,
                },
                {
                    date: "02",
                    outcome: -100,
                },
                {
                    date: "03",
                    outcome: -100,
                },
                ...emptyDates,
            ]);
        });

    test("selectAnalyticsForBarChart Daily. null data -> []", () => {
        const store = setupStore({
            categories: {
                categories,
                isInitialized: true,
            },
        });
        const result = selectAnalyticsForBarChart(
            store.getState(),
            1,
            AnalyticsPeriod.DAILY,
            "2021-01-10",
        );
        expect(result).toEqual([]);
    });

    test("selectAnalyticsForBarChart Daily. Categories no initialized -> work normal", () => {
        const store = setupStore({
            analytics,
        });
        const result = selectAnalyticsForBarChart(
            store.getState(),
            1,
            AnalyticsPeriod.DAILY,
            "2021-01-01",
        );
        expect(result).toEqual([
            {
                date: "01",
                income: 400,
                outcome: -100,
            },
            {
                date: "02",
                outcome: -100,
            },
            {
                date: "03",
                outcome: -100,
            },
            ...emptyDates,
        ]);
    });

    test("selectAnalyticsForBarChart Daily. Show categories && Categories not init -> []", () => {
        const store = setupStore({
            analytics,
        });
        const result = selectAnalyticsForBarChart(
            store.getState(),
            1,
            AnalyticsPeriod.DAILY,
            "2021-01-01",
            {
                showCategories: true,
                showChildCategories: false,
            },
        );
        expect(result).toEqual([]);
    });

    test("selectAnalyticsForBarChart Daily. ShowChildCategories & Categories not init -> []",
        () => {
            const store = setupStore({
                analytics,
            });
            const result = selectAnalyticsForBarChart(
                store.getState(),
                1,
                AnalyticsPeriod.DAILY,
                "2021-01-01",
                {
                    showCategories: true,
                    showChildCategories: true,
                },
            );
            expect(result).toEqual([]);
        });
});

const emptyDates = [
    { date: "04" },
    { date: "05" },
    { date: "06" },
    { date: "07" },
    { date: "08" },
    { date: "09" },
    { date: "10" },
    { date: "11" },
    { date: "12" },
    { date: "13" },
    { date: "14" },
    { date: "15" },
    { date: "16" },
    { date: "17" },
    { date: "18" },
    { date: "19" },
    { date: "20" },
    { date: "21" },
    { date: "22" },
    { date: "23" },
    { date: "24" },
    { date: "25" },
    { date: "26" },
    { date: "27" },
    { date: "28" },
    { date: "29" },
    { date: "30" },
    { date: "31" },
];
