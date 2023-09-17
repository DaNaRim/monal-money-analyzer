import { type LocalizedStrings } from "react-localization";
import { type Localization } from "../../i18n";
import { categoryColorMap } from "./categoryColorMap";
import { type Category } from "./categorySlice";

/**
 * Map categories to a flat array (If category has subcategories, they will be added to the array)
 *
 * @param categories array of all categories
 *
 * @returns array of all categories with subcategories in flat structure
 */
export const categoriesToFlatArray = (categories: Category[]): Category[] => {
    const categoriesWithSubcategories = [...categories];
    categories.forEach(category => {
        if (category.subCategories == null) {
            return;
        }
        category.subCategories.forEach(subCategory => {
            // if provided categories are already flat
            if (categoriesWithSubcategories.find(cur => cur.id === subCategory.id) != null) {
                return;
            }
            categoriesWithSubcategories.push(subCategory);
        });
    });
    return categoriesWithSubcategories;
};

/**
 * Finds parent category of given category if it exists.
 * If the category is null or parent not found, returns null.
 *
 * @param category category to find parent of (You can pass category itself or its name as a string)
 * @param categories array of all parent categories with subcategories or flat array of all
 *     categories
 *
 * @returns parent category of given category if it exists, null otherwise
 */
export const getParentCategory = (category: Category | string | null,
                                  categories: Category[],
): Category | null => {
    const parsedCategory = parseCategory(category, categories);

    if (parsedCategory == null) {
        return null;
    }
    let result = null;
    categoriesToFlatArray(categories)
        .forEach(cur => cur.subCategories?.forEach(subCategory => {
            if (subCategory.id === parsedCategory.id) {
                result = cur;
            }
        }));
    return result;
};

/**
 * Returns localized name of given category. If the category is null, returns "Deleted".
 *
 * @param category category to get name of (You can pass category itself or its name as a string)
 * @param categories array of all parent categories with subcategories or flat array of all
 *     categories
 * @param t localization object from {@link useTranslation} hook
 *
 * @returns localized name of given category
 */
export const getCategoryLocalName = (category: Category | string | null,
                                     categories: Category[],
                                     t: LocalizedStrings<Localization>,
): string => {
    if (typeof category === "string" && (category === "income" || category === "outcome")) {
        return t.getString(`data.transactionCategory.${category}Name`);
    }
    const parsedCategory = parseCategory(category, categories);

    if (parsedCategory == null
        || categoriesToFlatArray(categories).find(cur => cur.id === parsedCategory.id) == null) {
        return t.data.transactionCategory.deleted;
    }
    const categoryNameKey
        = parsedCategory.name.toLowerCase().replaceAll(" ", "_") ?? "";

    return category == null
        ? t.data.transactionCategory.deleted
        : t.getString(
            `data.transactionCategory.${parsedCategory.type.toLowerCase()}.${categoryNameKey}`,
        );
};

/**
 * Returns color of given category. If the category is null, returns deletedColor.
 *
 * @param category category to get color of (You can pass category itself or its name as a string)
 * @param categories array of all parent categories with subcategories or flat array of all
 *     categories
 *
 * @returns color of given category
 */
export const getColorByCategory = (category: Category | string | null,
                                   categories: Category[],
): string => {
    if (typeof category === "string" && (category === "income" || category === "outcome")) {
        return categoryColorMap[`${category}Color`];
    }
    const parsedCategory = parseCategory(category, categories);

    if (parsedCategory == null
        || categoriesToFlatArray(categories).find(cur => cur.id === parsedCategory.id) == null) {
        return categoryColorMap.deletedColor;
    }
    const categoryType = parsedCategory.type.toLowerCase();
    const categoryName = parsedCategory.name.toLowerCase().replaceAll(" ", "_");

    return categoryColorMap[categoryType as "income" | "outcome"][categoryName];
};

/**
 * Receives category as an object or string and parses it to object if it's a string.
 *
 * @param category category to parse
 * @param categories array of all parent categories with subcategories or flat array of all
 *     categories
 *
 * @returns parsed category or null if category is null or parsed category is not found
 */
const parseCategory = (category: Category | string | null,
                       categories: Category[],
): Category | null => {
    if (typeof category === "object") {
        return category;
    }
    const foundCategory = categoriesToFlatArray(categories).find(cur => cur.name === category);

    if (category == null || foundCategory == null) {
        return null;
    }
    return foundCategory;
};
