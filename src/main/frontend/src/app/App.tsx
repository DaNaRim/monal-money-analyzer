import React from "react";
import {Route, Routes, useLocation} from "react-router";
import ErrorPage from "../common/pages/error/ErrorPage/ErrorPage";
import ForbiddenPage from "../common/pages/error/ForbiddenPage/ForbiddenPage";
import NotFoundPage from "../common/pages/error/NotFoundPage/NotFoundPage";
import HomePage from "../common/pages/HomePage/HomePage";
import LoginPage from "../common/pages/LoginPage/LoginPage";
import RegistrationPage from "../common/pages/RegistrationPage/RegistrationPage";
import ResendVerificationTokenPage from "../common/pages/ResendVerificationTokenPage/ResendVerificationTokenPage";
import ResetPasswordPage from "../common/pages/ResetPasswordPage/ResetPasswordPage";
import ResetPasswordSetPage from "../common/pages/ResetPasswordSetPage/ResetPasswordSetPage";
import {checkForServerMessages} from "../features/appMessages/appMessagesSlice";
import {selectAuthIsForceLogin, setForceLogin} from "../features/auth/authSlice";
import "./App.scss";
import {useAppDispatch, useAppSelector} from "./hooks";

const App = () => {
    const dispatch = useAppDispatch();
    const location = useLocation();

    const isForceLoin = useAppSelector<boolean>(selectAuthIsForceLogin);

    if (location.pathname !== "/login" && isForceLoin) {
        dispatch(setForceLogin(false));
    }
    dispatch(checkForServerMessages());

    return (
        <Routes>
            <Route path="/" element={<HomePage/>}/>
            <Route path="/registration" element={<RegistrationPage/>}/>
            <Route path="/login" element={<LoginPage/>}/>
            <Route path="/resendVerificationToken" element={<ResendVerificationTokenPage/>}/>
            <Route path="/resetPassword" element={<ResetPasswordPage/>}/>
            <Route path="/resetPasswordSet" element={<ResetPasswordSetPage/>}/>

            <Route path="/forbidden" element={<ForbiddenPage/>}/>
            <Route path="/error" element={<ErrorPage/>}/>
            <Route path="*" element={<NotFoundPage/>}/>
        </Routes>
    );
};

export default App;
