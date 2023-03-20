import React, {useEffect} from "react";
import {Route, Routes, useLocation} from "react-router";
import PageWrapper from "../common/components/base/PageWrapper/PageWrapper";
import {checkForServerMessages} from "../features/appMessages/appMessagesSlice";
import {selectAuthIsForceLogin, setForceLogin} from "../features/auth/authSlice";
import "./App.scss";
import {useAppDispatch, useAppSelector} from "./hooks/reduxHooks";

const HomePage = React.lazy(() => import("../common/pages/HomePage/HomePage"));
const RegistrationPage = React.lazy(() => import("../common/pages/RegistrationPage/RegistrationPage"));
const LoginPage = React.lazy(() => import("../common/pages/LoginPage/LoginPage"));
const ResendVerificationTokenPage = React.lazy(() => import("../common/pages/ResendVerificationTokenPage/ResendVerificationTokenPage"));
const ResetPasswordPage = React.lazy(() => import("../common/pages/ResetPasswordPage/ResetPasswordPage"));
const ResetPasswordSetPage = React.lazy(() => import("../common/pages/ResetPasswordSetPage/ResetPasswordSetPage"));

const ForbiddenPage = React.lazy(() => import("../common/pages/error/ForbiddenPage/ForbiddenPage"));
const ErrorPage = React.lazy(() => import("../common/pages/error/ErrorPage/ErrorPage"));
const NotFoundPage = React.lazy(() => import("../common/pages/error/NotFoundPage/NotFoundPage"));

const App = () => {
    const dispatch = useAppDispatch();
    const location = useLocation();

    const isForceLoin = useAppSelector<boolean>(selectAuthIsForceLogin);

    if (location.pathname !== "/login" && isForceLoin) {
        dispatch(setForceLogin(false));
    }

    useEffect(() => {
        dispatch(checkForServerMessages());
    }, [dispatch]);

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
