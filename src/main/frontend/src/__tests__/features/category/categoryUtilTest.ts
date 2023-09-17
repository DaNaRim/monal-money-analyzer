import { describe } from "@jest/globals";
import { type LocalizedStrings } from "react-localization";
import { type Category, CategoryType } from "../../../features/category/categorySlice";
import {
    categoriesToFlatArray,
    getCategoryLocalName,
    getColorByCategory,
    getParentCategory,
} from "../../../features/category/categoryUtil";
import { type Localization } from "../../../i18n";

const subCategory: Category = {
    id: 2,
    name: "Subcategory 1",
    type: CategoryType.INCOME,
    subCategories: null,
};

const categories: Category[] = [
    {
        id: 1,
        name: "Category 1",
        type: CategoryType.INCOME,
        subCategories: [subCategory],
    },
    {
        id: 3,
        name: "Category 2",
        type: CategoryType.OUTCOME,
        subCategories: null,
    },
];

const flatCategories: Category[] = [
    {
        id: 1,
        name: "Category 1",
        type: CategoryType.INCOME,
        subCategories: [subCategory],
    },
    {
        id: 3,
        name: "Category 2",
        type: CategoryType.OUTCOME,
        subCategories: null,
    },
    subCategory,
];

const getStringMockImplementation = (key: string) => {
    if (key === "data.transactionCategory.outcome.category_2") {
        return "Category 2 result";
    }
    if (key === "data.transactionCategory.income.subcategory_1") {
        return "Subcategory 1 result";
    }
    if (key === "data.transactionCategory.incomeName") {
        return "Income result";
    }
    if (key === "data.transactionCategory.outcomeName") {
        return "Outcome result";
    }
    return "";
};

const getStringMock = jest.fn();

// Translation object for tests
const t = {
    getString: getStringMock,
    data: {
        transactionCategory: {
            deleted: "Deleted",
        },
    },
} as unknown as LocalizedStrings<Localization>;

jest.mock("../../../features/category/categoryColorMap", () => ({
    categoryColorMap: {
        incomeColor: "green",
        outcomeColor: "red",
        deletedColor: "black",
        income: {
            subcategory_1: "blue",
        },
    },
}));

describe("categoryUtil", () => {
    beforeEach(() => jest.resetModules());

    // categoriesToFlatArray

    test("categoriesToFlatArray", () => {
        expect(categoriesToFlatArray(categories)).toEqual(flatCategories);
    });

    test("categoriesToFlatArray. Already Flat -> No change", () => {
        expect(categoriesToFlatArray(flatCategories)).toEqual(flatCategories);
    });

    // getParentCategory

    test("getParentCategory", () => {
        expect(getParentCategory(subCategory, categories)).toEqual({
            id: 1,
            name: "Category 1",
            type: CategoryType.INCOME,
            subCategories: [subCategory],
        });
    });

    test("getParentCategory. No parent -> null", () => {
        const categoryToFind: Category = {
            id: 3,
            name: "Category 2",
            type: CategoryType.OUTCOME,
            subCategories: null,
        };
        expect(getParentCategory(categoryToFind, categories)).toBeNull();
    });

    test("getParentCategory. string category", () => {
        expect(getParentCategory("Subcategory 1", categories)).toEqual({
            id: 1,
            name: "Category 1",
            type: CategoryType.INCOME,
            subCategories: [subCategory],
        });
    });

    test("getParentCategory. null category -> null", () => {
        expect(getParentCategory(null, categories)).toBeNull();
    });

    test("getParentCategory. category not found-> null", () => {
        const differentCategory: Category = {
            id: 4,
            name: "Category 4",
            type: CategoryType.OUTCOME,
            subCategories: null,
        };
        expect(getParentCategory(differentCategory, categories)).toBeNull();
    });

    test("getParentCategory. string category not found-> null", () => {
        expect(getParentCategory("Category 4", categories)).toBeNull();
    });

    // getCategoryLocalName

    test("getCategoryLocalName", () => {
        getStringMock.mockImplementation(getStringMockImplementation);

        expect(getCategoryLocalName(subCategory, categories, t)).toEqual("Subcategory 1 result");
        expect(getStringMock).toBeCalledWith("data.transactionCategory.income.subcategory_1");
    });

    test("getCategoryLocalName. string category", () => {
        getStringMock.mockImplementation(getStringMockImplementation);

        expect(getCategoryLocalName("Category 2", categories, t)).toEqual("Category 2 result");
        expect(getStringMock).toBeCalledWith("data.transactionCategory.outcome.category_2");
    });

    test("getCategoryLocalName. null category -> deleted", () => {
        getStringMock.mockImplementation(getStringMockImplementation);

        expect(getCategoryLocalName(null, categories, t)).toEqual("Deleted");
    });

    test("getCategoryLocalName. Category not found -> deleted", () => {
        getStringMock.mockImplementation(getStringMockImplementation);

        expect(getCategoryLocalName("Category qwe", categories, t)).toEqual("Deleted");
    });

    test("getCategoryLocalName. income", () => {
        getStringMock.mockImplementation(getStringMockImplementation);

        expect(getCategoryLocalName("income", categories, t)).toEqual("Income result");
        expect(getStringMock).toBeCalledWith("data.transactionCategory.incomeName");
    });

    test("getCategoryLocalName. outcome", () => {
        getStringMock.mockImplementation(getStringMockImplementation);

        expect(getCategoryLocalName("outcome", categories, t)).toEqual("Outcome result");
        expect(getStringMock).toBeCalledWith("data.transactionCategory.outcomeName");
    });

    test("getCategoryLocalName. category not in categories -> deleted", () => {
        getStringMock.mockImplementation(getStringMockImplementation);

        expect(getCategoryLocalName("Category 4", categories, t)).toEqual("Deleted");
    });

    // getColorByCategory

    test("getColorByCategory", () => {
        expect(getColorByCategory(subCategory, categories)).toEqual("blue");
    });

    test("getColorByCategory. string category", () => {
        expect(getColorByCategory("Subcategory 1", categories)).toEqual("blue");
    });

    test("getColorByCategory. null category -> deletedColor", () => {
        expect(getColorByCategory(null, categories)).toEqual("black");
    });

    test("getColorByCategory. Category not found -> deletedColor", () => {
        expect(getColorByCategory("Category 4", categories)).toEqual("black");
    });

    test("getColorByCategory. income", () => {
        expect(getColorByCategory("income", categories)).toEqual("green");
    });

    test("getColorByCategory. outcome", () => {
        expect(getColorByCategory("outcome", categories)).toEqual("red");
    });
});
