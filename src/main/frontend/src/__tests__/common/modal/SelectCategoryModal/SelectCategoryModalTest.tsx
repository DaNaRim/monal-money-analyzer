import { describe } from "@jest/globals";
import { screen } from "@testing-library/dom";
import { act, fireEvent } from "@testing-library/react";
import { setupStore } from "../../../../app/store";
import SelectCategoryModal from "../../../../common/modal/SelectCategoryModal/SelectCategoryModal";
import { renderWithProviders } from "../../../../common/utils/test-utils";
import { type Category, CategoryType } from "../../../../features/category/categorySlice";

export const testCategories: Category[] = [
    {
        id: 1,
        name: "Food and beverages",
        type: CategoryType.OUTCOME,
        subCategories: [
            {
                id: 2,
                name: "Grocery",
                type: CategoryType.OUTCOME,
                subCategories: [],
            },
            {
                id: 3,
                name: "Restaurant",
                type: CategoryType.OUTCOME,
                subCategories: [],
            },
            {
                id: 4,
                name: "Cafe",
                type: CategoryType.OUTCOME,
                subCategories: [],
            },
        ],
    },
    {
        id: 5,
        name: "Salary",
        type: CategoryType.INCOME,
        subCategories: null,
    },
];

describe("SelectCategoryModal", () => {
    it("render", () => {
        const store = setupStore({
            categories: {
                categories: testCategories,
                isInitialized: true,
            },
        });
        renderWithProviders(<SelectCategoryModal open={true}
                                                 setOpen={jest.fn()}
                                                 setCategory={jest.fn()}/>, { store });

        // Tabs
        expect(screen.getByText("Income")).toBeInTheDocument();
        expect(screen.getByText("Outcome")).toBeInTheDocument();
        expect(screen.getByText("Outcome")).toHaveAttribute("aria-selected", "true");

        // Categories
        expect(screen.getByText("Food and beverages")).toBeInTheDocument();
        expect(screen.getByText("Grocery")).toBeInTheDocument();
        expect(screen.getByText("Restaurant")).toBeInTheDocument();
        // Should be in show more
        expect(screen.queryByText("Cafe")).not.toBeInTheDocument();
        expect(screen.getByText("Show more")).toBeInTheDocument();

        expect(screen.queryByText("Salary")).not.toBeInTheDocument();
    });

    it("select Income tab", async () => {
        const store = setupStore({
            categories: {
                categories: testCategories,
                isInitialized: true,
            },
        });
        renderWithProviders(<SelectCategoryModal open={true}
                                                 setOpen={jest.fn()}
                                                 setCategory={jest.fn()}/>, { store });

        await act(() => fireEvent.click(screen.getByText("Income")));

        expect(screen.getByText("Income")).toHaveAttribute("aria-selected", "true");

        expect(screen.getByText("Salary")).toBeInTheDocument();
        expect(screen.queryByText("Show more")).not.toBeInTheDocument();

        expect(screen.queryByText("Food and beverages")).not.toBeInTheDocument();
        expect(screen.queryByText("Grocery")).not.toBeInTheDocument();
        expect(screen.queryByText("Restaurant")).not.toBeInTheDocument();
    });

    it("select category", async () => {
        const setOpen = jest.fn();
        const setCategory = jest.fn();

        const store = setupStore({
            categories: {
                categories: testCategories,
                isInitialized: true,
            },
        });
        renderWithProviders(<SelectCategoryModal open={true}
                                                 setOpen={setOpen}
                                                 setCategory={setCategory}/>, { store });

        await act(() => fireEvent.click(screen.getByText("Food and beverages")));

        expect(setCategory).toBeCalledWith(testCategories[0]);
        expect(setOpen).toBeCalledWith(false);
    });

    it("select subcategory", async () => {
        const setOpen = jest.fn();
        const setCategory = jest.fn();

        const store = setupStore({
            categories: {
                categories: testCategories,
                isInitialized: true,
            },
        });
        renderWithProviders(<SelectCategoryModal open={true}
                                                 setOpen={setOpen}
                                                 setCategory={setCategory}/>, { store });

        await act(() => fireEvent.click(screen.getByText("Grocery")));

        expect(setCategory).toBeCalledWith(testCategories[0].subCategories?.[0]);
        expect(setOpen).toBeCalledWith(false);
    });

    it("Click on show more", async () => {
        const setOpen = jest.fn();
        const setCategory = jest.fn();

        const store = setupStore({
            categories: {
                categories: testCategories,
                isInitialized: true,
            },
        });
        renderWithProviders(<SelectCategoryModal open={true}
                                                 setOpen={setOpen}
                                                 setCategory={setCategory}/>, { store });

        await act(() => fireEvent.click(screen.getByText("Show more")));

        expect(screen.getByText("Food and beverages")).toBeInTheDocument();
        expect(screen.getByText("Grocery")).toBeInTheDocument();
        expect(screen.getByText("Restaurant")).toBeInTheDocument();
        expect(screen.getByText("Cafe")).toBeInTheDocument();
        expect(screen.getByText("Show less")).toBeInTheDocument();

        expect(screen.queryByText("Salary")).not.toBeInTheDocument();

        expect(setOpen).not.toBeCalled();
        expect(setCategory).not.toBeCalled();
    });

    it("Click on show more and show less", async () => {
        const setOpen = jest.fn();
        const setCategory = jest.fn();

        const store = setupStore({
            categories: {
                categories: testCategories,
                isInitialized: true,
            },
        });
        renderWithProviders(<SelectCategoryModal open={true}
                                                 setOpen={setOpen}
                                                 setCategory={setCategory}/>, { store });

        await act(() => fireEvent.click(screen.getByText("Show more")));
        await act(() => fireEvent.click(screen.getByText("Show less")));

        expect(screen.getByText("Food and beverages")).toBeInTheDocument();
        expect(screen.getByText("Grocery")).toBeInTheDocument();
        expect(screen.getByText("Restaurant")).toBeInTheDocument();
        expect(screen.queryByText("Cafe")).not.toBeInTheDocument();
        expect(screen.getByText("Show more")).toBeInTheDocument();

        expect(setOpen).not.toBeCalled();
        expect(setCategory).not.toBeCalled();
    });
});
