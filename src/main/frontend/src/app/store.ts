import {
    type Action,
    combineReducers,
    configureStore,
    type PreloadedState,
    type ThunkAction,
} from "@reduxjs/toolkit";
import { apiSlice } from "../features/api/apiSlice";
import appMessagesReducer from "../features/appMessages/appMessagesSlice";
import authReducer from "../features/auth/authSlice";

const isDev = process.env.NODE_ENV === "development";

const rootReducer = combineReducers({
    [apiSlice.reducerPath]: apiSlice.reducer,
    auth: authReducer,
    appMessages: appMessagesReducer,
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
