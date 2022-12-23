import React from "react";
import {Route, Routes} from "react-router";
import HomePage from "../common/pages/Home/HomePage";
import LoginPage from "../common/pages/Login/LoginPage";
import NotFoundPage from "../common/pages/NotFoundPage/NotFoundPage";
import RegistrationPage from "../common/pages/Registration/RegistrationPage";
import ResendVerificationTokenPage from "../common/pages/ResendVerificationTokenPage/ResendVerificationTokenPage";
import ResetPasswordPage from "../common/pages/ResetPasswordPage/ResetPasswordPage";
import ResetPasswordSetPage from "../common/pages/ResetPasswordSetPage/ResetPasswordSetPage";

import {checkForServerMessages} from "../features/appMessages/appMessagesSlice";

import "./App.scss";
import {useAppDispatch} from "./hooks";

const App = () => {
    const dispatch = useAppDispatch();
    dispatch(checkForServerMessages());

    return (
        <Routes>
            <Route path="/" element={<HomePage/>}/>
            <Route path="/registration" element={<RegistrationPage/>}/>
            <Route path="/login" element={<LoginPage/>}/>
            <Route path="/resendVerificationToken" element={<ResendVerificationTokenPage/>}/>
            <Route path="/resetPassword" element={<ResetPasswordPage/>}/>
            <Route path="/resetPasswordSet" element={<ResetPasswordSetPage/>}/>

            <Route path="*" element={<NotFoundPage/>}/>
        </Routes>
    );
};

export default App;
