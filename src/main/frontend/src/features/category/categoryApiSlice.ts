import { apiSlice } from "../api/apiSlice";
import { type Category } from "./categorySlice";

const categoryApiSlice = apiSlice.injectEndpoints({
    endpoints: builder => ({
        getCategories: builder.query<Category[], void>({
            query: () => ({
                url: "/categories",
                method: "GET",
            }),
        }),
    }),
});

export const {
    useGetCategoriesQuery,
} = categoryApiSlice;

export default categoryApiSlice.reducer;
