import {apiSlice} from "@features/api/apiSlice";
import appMessagesReducer from "@features/appMessages/appMessagesSlice";
import authReducer from "@features/auth/authSlice";
import {Action, configureStore, ThunkAction} from "@reduxjs/toolkit";

const isDev = process.env.NODE_ENV === "development";

export const store = configureStore({
    reducer: {
        [apiSlice.reducerPath]: apiSlice.reducer,
        auth: authReducer,
        appMessages: appMessagesReducer,
    },
    middleware: getDefaultMiddleware => getDefaultMiddleware()
        .concat(apiSlice.middleware),
    devTools: isDev,
});

export type AppDispatch = typeof store.dispatch;
export type RootState = ReturnType<typeof store.getState>;
export type AppThunk<ReturnType = void> = ThunkAction<ReturnType,
    RootState,
    unknown,
    Action<string>>;
