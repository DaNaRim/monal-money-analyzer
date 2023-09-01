import { describe } from "@jest/globals";
import { act, fireEvent, screen } from "@testing-library/react";
import dayjs from "dayjs";
import { setupStore } from "../../../../../app/store";
import AnalyticsBlock from "../../../../../common/components/money/AnalyticsBlock/AnalyticsBlock";
import { renderWithProviders } from "../../../../../common/utils/test-utils";
import { type Category, CategoryType } from "../../../../../features/category/categorySlice";

// Nivo pie does not render in tests. So, I don't know how to test it.

describe("AnalyticsBlock", () => {
    it("render", () => {
        const category: Category = {
            id: 1,
            name: "Food and beverages",
            type: CategoryType.OUTCOME,
            subCategories: [],
        };
        const store = setupStore({
            transactions: {
                1: {
                    "2021-01-01": [
                        {
                            id: 1,
                            category,
                            date: dayjs("2021-01-01").toDate(),
                            amount: 100,
                            description: "Description 1",
                        },
                        {
                            id: 2,
                            category,
                            date: dayjs("2021-01-01").toDate(),
                            amount: 120,
                            description: "Description 2",
                        },
                    ],
                },
            },
        });
        renderWithProviders(<AnalyticsBlock walletId={1} date="2021-01-01"/>, { store });
        // Tabs
        expect(screen.getByText("Outcome")).toBeInTheDocument();
        expect(screen.getByText("Income")).toBeInTheDocument();
        expect(screen.getByText("Outcome")).toHaveAttribute("aria-selected", "true");
        // Checkbox
        expect(screen.getByText("Show child categories")).toBeInTheDocument();
        // Total amount
        expect(screen.getByText("220")).toBeInTheDocument();
        expect(screen.getByText("0")).toBeInTheDocument();
    });

    it("render no transactions -> display no analytics", () => {
        renderWithProviders(<AnalyticsBlock walletId={1} date="2021-01-01"/>);
        // Tabs
        expect(screen.getByText("Outcome")).toBeInTheDocument();
        expect(screen.getByText("Income")).toBeInTheDocument();
        // Checkbox - not displayed
        expect(screen.queryByText("Show child categories")).not.toBeInTheDocument();
        // Total amount
        expect(screen.getAllByText("0")).toHaveLength(2);
        // Message
        expect(screen.getByText("No transactions for analytics")).toBeInTheDocument();
    });

    it("switch tabs", async () => {
        const category: Category = {
            id: 1,
            name: "Food and beverages",
            type: CategoryType.OUTCOME,
            subCategories: [],
        };
        const store = setupStore({
            transactions: {
                1: {
                    "2021-01-01": [
                        {
                            id: 1,
                            category,
                            date: dayjs("2021-01-01").toDate(),
                            amount: 100,
                            description: "Description 1",
                        },
                    ],
                },
            },
        });
        renderWithProviders(<AnalyticsBlock walletId={1} date="2021-01-01"/>, { store });

        expect(screen.queryByText("No transactions for analytics")).not.toBeInTheDocument();

        await act(() => fireEvent.click(screen.getByText("Income")));
        expect(screen.getByText("No transactions for analytics")).toBeInTheDocument();
        expect(screen.getByText("100")).toBeInTheDocument();
        expect(screen.getByText("0")).toBeInTheDocument();
    });
});
