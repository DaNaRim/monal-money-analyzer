/**
 * This file contains a map of colors for categories and subcategories.
 * It is used in {@link getColorByCategory} function.
 */

export interface CategoryColorMapType {
    incomeColor: string; // For basic income
    outcomeColor: string; // For basic outcome
    deletedColor: string; // If category is not found
    outcome: Record<string, string>;
    income: Record<string, string>;
}

export const categoryColorMap: CategoryColorMapType = {
    incomeColor: "#0f0",
    outcomeColor: "#f00",
    deletedColor: "#490000",
    outcome: {
        food_and_beverages: "#ffed00",
        grocery: "#ffcaca",
        restaurant: "#68ff2d",
        cafe: "#ff9e00",
        alcohol_and_bars: "#f0f",
        snacks: "#1481ff",

        transportation: "#0f0",
        public_transportation: "#f0f",
        taxi: "#ff9e00",
        gas: "#f00",
        parking: "#ffcaca",
        car_warranty: "#1481ff",
        car_maintenance: "#68ff2d",

        travel: "#ff9e00",

        entertainment: "#f0f",
        movies: "#ffcaca",
        concerts: "#f00",
        theater: "#68ff2d",
        games: "#1481ff",

        family: "#ffcaca",
        partner: "#f00",
        children: "#ff9e00",
        parents: "#68ff2d",
        pets: "#1481ff",

        friends: "#ff9e00",

        hobby: "#f00",

        sport: "#ffcaca",
        gym: "#ff9e00",
        sport_equipment: "#68ff2d",

        personal_care: "#f0f",
        haircut: "#ffcaca",
        beauty: "#ff9e00",
        cosmetics: "#68ff2d",
        spa: "#1481ff",

        health: "#f00",
        pharmacy: "#ffcaca",
        primary_care: "#ff9e00",
        dental_care: "#68ff2d",
        specialty_care: "#1481ff",
        surgery: "#f0f",
        medical_devices: "#ffcaca",

        education: "#ff9e00",
        books: "#f00",
        courses: "#ffcaca",

        shopping: "#f0f",
        clothing: "#ffcaca",
        shoes: "#ff9e00",
        electronics: "#68ff2d",
        accessories: "#1481ff",
        home: "#f00",

        bills: "#ffcaca",
        subscription: "#ff9e00",
        phone_bill: "#68ff2d",
        internet_bill: "#1481ff",
        television_bill: "#f0f",
        rent: "#f00",
        watter_bill: "#ffcaca",
        electricity_bill: "#ff9e00",
        gas_bill: "#68ff2d",

        gift: "#f0f",
        birthday: "#ffcaca",
        charity: "#ff9e00",

        business: "#f00",

        savings: "#ffcaca",

        other: "#f0f",
    },
    income: {
        salary: "#f00",
        gift: "#ffcaca",
        award: "#ff9e00",
        sponsorship: "#68ff2d",
        business: "#1481ff",
        other: "#ff00ff",
    },
};
