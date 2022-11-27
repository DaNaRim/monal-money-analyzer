import React from "react";
import {Route, Routes} from "react-router";
import HomePage from "../common/pages/Home/HomePage";
import RegistrationPage from "../common/pages/Registration/RegistrationPage";
import LoginPage from "../common/pages/Login/LoginPage";

import "./App.scss";

const App = () => (
    <Routes>
        <Route path="/" element={<HomePage/>}/>
        <Route path="/registration" element={<RegistrationPage/>}/>
        <Route path="/login" element={<LoginPage/>}/>
    </Routes>
);

export default App;
