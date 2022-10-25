import React from "react";
import "./App.css";
import {Route, Routes} from "react-router";
import HomePage from "./common/pages/Home/HomePage";
import RegistrationPage from "./common/pages/Registration/RegistrationPage";

const App = () => (
    <Routes>
        <Route path="/" element={<HomePage/>}/>
        <Route path="/registration" element={<RegistrationPage/>}/>
    </Routes>
);

export default App;
