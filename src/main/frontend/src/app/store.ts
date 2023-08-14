import {
    type Action,
    combineReducers,
    configureStore,
    type PreloadedState,
    type ThunkAction,
} from "@reduxjs/toolkit";
import { apiSlice, redirectSlice } from "../features/api/apiSlice";
import appMessagesReducer from "../features/appMessages/appMessagesSlice";
import authReducer from "../features/auth/authSlice";
import categorySlice from "../features/category/categorySlice";
import transactionSlice from "../features/transaction/transactionSlice";
import walletSlice from "../features/wallet/walletSlice";

const isDev = process.env.NODE_ENV === "development";

const rootReducer = combineReducers({
    [apiSlice.reducerPath]: apiSlice.reducer,
    redirect: redirectSlice.reducer,
    auth: authReducer,
    appMessages: appMessagesReducer,
    wallet: walletSlice,
    categories: categorySlice,
    wallets: walletSlice,
    transactions: transactionSlice,
});

export const setupStore = (preloadedState?: PreloadedState<RootState>) =>
    configureStore({
        reducer: rootReducer,
        middleware: getDefaultMiddleware => getDefaultMiddleware()
            .concat(apiSlice.middleware),
        devTools: isDev,
        preloadedState,
    });

export type AppStore = ReturnType<typeof setupStore>;
export type AppDispatch = AppStore["dispatch"];
export type RootState = ReturnType<typeof rootReducer>;
export type AppThunk<ReturnType = void> = ThunkAction<
    ReturnType,
    RootState,
    unknown,
    Action<string>
>;
