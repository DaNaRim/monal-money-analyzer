import { describe } from "@jest/globals";
import { setupStore } from "../../../app/store";
import reducer, {
    type Category,
    type CategoryState,
    CategoryType,
    selectCategoriesWithSubcategories,
    setCategories,
} from "../../../features/category/categorySlice";

describe("categorySlice", () => {
    test("init state", () => {
        expect(reducer(undefined, { type: undefined })).toEqual({
            categories: [],
            isInitialized: false,
        });
    });

    test("setCategories", () => {
        const categories: Category[] = [
            {
                id: 1,
                name: "Category 1",
                type: CategoryType.INCOME,
                subCategories: [
                    {
                        id: 2,
                        name: "Subcategory 1",
                        type: CategoryType.INCOME,
                        subCategories: null,
                    },
                ],
            },
            {
                id: 3,
                name: "Category 2",
                type: CategoryType.OUTCOME,
                subCategories: null,
            },
        ];
        expect(reducer(undefined, setCategories(categories))).toEqual({
            categories,
            isInitialized: true,
        });
    });

    test("selectCategoriesWithSubcategories", () => {
        const preloadedState: CategoryState = {
            categories: [
                {
                    id: 1,
                    name: "Category 1",
                    type: CategoryType.INCOME,
                    subCategories: [
                        {
                            id: 2,
                            name: "Subcategory 1",
                            type: CategoryType.INCOME,
                            subCategories: null,
                        },
                    ],
                },
                {
                    id: 3,
                    name: "Category 2",
                    type: CategoryType.OUTCOME,
                    subCategories: null,
                },
            ],
            isInitialized: true,
        };
        const store = setupStore({
            categories: preloadedState,
        });

        const result = selectCategoriesWithSubcategories(store.getState());
        expect(result).toEqual([
            {
                id: 1,
                name: "Category 1",
                type: CategoryType.INCOME,
                subCategories: expect.any(Array),
            },
            {
                id: 3,
                name: "Category 2",
                type: CategoryType.OUTCOME,
                subCategories: null,
            },
            {
                id: 2,
                name: "Subcategory 1",
                type: CategoryType.INCOME,
                subCategories: null,
            },
        ]);
    });
});
