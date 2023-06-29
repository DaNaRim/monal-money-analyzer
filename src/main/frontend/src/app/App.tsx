import React, { useEffect } from "react";
import { Route, Routes, useNavigate } from "react-router";
import PageWrapper from "../common/components/base/PageWrapper/PageWrapper";
import { clearRedirect, selectRedirectTo } from "../features/api/apiSlice";
import { checkForServerMessages } from "../features/appMessages/appMessagesSlice";
import "./App.scss";
import { useAppDispatch, useAppSelector } from "./hooks/reduxHooks";

const HomePage = React.lazy(async () => await import("../common/pages/HomePage/HomePage"));
const RegistrationPage = React.lazy(async () =>
    await import("../common/pages/RegistrationPage/RegistrationPage"),
);
const LoginPage = React.lazy(async () =>
    await import("../common/pages/LoginPage/LoginPage"),
);
const ResendVerificationTokenPage = React.lazy(async () =>
    await import("../common/pages/ResendVerificationTokenPage/ResendVerificationTokenPage"),
);
const ResetPasswordPage = React.lazy(async () =>
    await import("../common/pages/ResetPasswordPage/ResetPasswordPage"),
);
const ResetPasswordSetPage = React.lazy(async () =>
    await import("../common/pages/ResetPasswordSetPage/ResetPasswordSetPage"),
);

const ForbiddenPage = React.lazy(async () =>
    await import("../common/pages/error/ForbiddenPage/ForbiddenPage"),
);
const ErrorPage = React.lazy(async () =>
    await import("../common/pages/error/ErrorPage/ErrorPage"),
);
const NotFoundPage = React.lazy(async () =>
    await import("../common/pages/error/NotFoundPage/NotFoundPage"),
);

const App = (): JSX.Element => {
    const dispatch = useAppDispatch();
    const navigate = useNavigate();

    const redirectTo = useAppSelector<string | null>(selectRedirectTo);

    useEffect(() => {
        dispatch(checkForServerMessages());
    }, [dispatch]);

    // Used for redirecting from apiSlice (auth refresh)
    useEffect(() => {
        if (redirectTo !== null) {
            navigate(redirectTo);
        }
        dispatch(clearRedirect());
    }, [dispatch, navigate, redirectTo]);

    return (
        <Routes>
            <Route path="/" element={<PageWrapper/>}>
                <Route path="/" index element={<HomePage/>}/>
                <Route path="/registration" element={<RegistrationPage/>}/>
                <Route path="/login" element={<LoginPage/>}/>
                <Route path="/resendVerificationToken" element={<ResendVerificationTokenPage/>}/>
                <Route path="/resetPassword" element={<ResetPasswordPage/>}/>
                <Route path="/resetPasswordSet" element={<ResetPasswordSetPage/>}/>

                <Route path="/forbidden" element={<ForbiddenPage/>}/>
                <Route path="/error" element={<ErrorPage/>}/>
                <Route path="*" element={<NotFoundPage/>}/>
            </Route>
        </Routes>
    );
};

export default App;
