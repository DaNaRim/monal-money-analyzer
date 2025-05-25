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
    incomeColor: "#6dbf4b",      // medium green
    outcomeColor: "#e04b4b",     // soft red
    deletedColor: "#6b1f1f",     // dark muted red-brown

    outcome: {
        food_and_beverages: "#f7c948",
        grocery: "#fbe29f",
        restaurant: "#e0aa3e",
        cafe: "#d48f00",
        alcohol_and_bars: "#b57600",
        snacks: "#a86100",

        transportation: "#68b3c8",
        public_transportation: "#84c9db",
        taxi: "#5ca1b2",
        gas: "#3b7c91",
        parking: "#2a5e6e",
        car_warranty: "#75c1d1",
        car_maintenance: "#4b9fb1",

        travel: "#90c370",

        entertainment: "#cf75d5",
        movies: "#e4a5e8",
        concerts: "#b957c2",
        theater: "#9a34a7",
        games: "#7d1a8f",

        family: "#f5a6a6",
        partner: "#eb7d7d",
        children: "#f18f64",
        parents: "#e56c6c",
        pets: "#d94b4b",

        friends: "#f7b764",

        hobby: "#eb6cb1",

        sport: "#6dbfcd",
        gym: "#4db2c0",
        sport_equipment: "#3ba5b4",

        personal_care: "#e197c2",
        haircut: "#f5bfdc",
        beauty: "#d27fa6",
        cosmetics: "#b8628c",
        spa: "#9e476f",

        health: "#e76060",
        pharmacy: "#f08d8d",
        primary_care: "#e05d5d",
        dental_care: "#c74a4a",
        specialty_care: "#ad3a3a",
        surgery: "#933131",
        medical_devices: "#b66868",

        education: "#a4c48c",
        books: "#85a86b",
        courses: "#c2d8ae",

        shopping: "#7a6edc",
        clothing: "#b4acef",
        shoes: "#9a8ee2",
        electronics: "#806cd5",
        accessories: "#6650c9",
        home: "#4f38b0",

        bills: "#eabf7b",
        subscription: "#e3a657",
        phone_bill: "#d98d32",
        internet_bill: "#bf7321",
        television_bill: "#a45a0f",
        rent: "#8a4700",
        watter_bill: "#d7ae78",
        electricity_bill: "#c99656",
        gas_bill: "#ab6c2e",

        gift: "#dfa9e3",
        birthday: "#f5d2f7",
        charity: "#c98ccf",

        business: "#568ea6",

        savings: "#9ec6b4",

        other: "#d3a7f4",
    },

    income: {
        salary: "#7ac576",
        gift: "#b3e2b0",
        award: "#a2d699",
        sponsorship: "#89c281",
        business: "#71ae69",
        other: "#5a9b52",
    },
};

