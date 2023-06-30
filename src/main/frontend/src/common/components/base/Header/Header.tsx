import React, { useEffect } from "react";
import { useNavigate } from "react-router";
import { NavLink } from "react-router-dom";
import { useAppDispatch, useAppSelector } from "../../../../app/hooks/reduxHooks";
import useTranslation from "../../../../app/hooks/translation";
import { useAuthGetStateMutation, useLogoutMutation } from "../../../../features/auth/authApiSlice";
import {
    clearAuthState,
    selectAuthFirstname,
    selectAuthIsInitialized,
    selectAuthLastname,
    selectAuthUsername,
    setCredentials,
    setInitialized,
} from "../../../../features/auth/authSlice";
import LanguageHandler from "../LanguageHandler/LanguageHandler";

import styles from "./Header.module.scss";

const Header = () => {
    const dispatch = useAppDispatch();
    const navigate = useNavigate();
    const t = useTranslation();

    const username = useAppSelector(selectAuthUsername);
    const firstName = useAppSelector(selectAuthFirstname);
    const lastName = useAppSelector(selectAuthLastname);

    const isAuthInit = useAppSelector(selectAuthIsInitialized);

    const [getAuthState, { isLoading: isAuthStateLoading }] = useAuthGetStateMutation();
    const [logout, { isLoading: isLogoutLoading }] = useLogoutMutation();

    const handleLogout = () => {
        void logout();
        dispatch(clearAuthState());
        navigate("/login");
    };

    useEffect(() => {
        if (isAuthInit) {
            return;
        }
        getAuthState().unwrap()
            .then(res => dispatch(setCredentials(res)))
            .catch(() => dispatch(setInitialized()));
    }, [dispatch, getAuthState, isAuthInit]);

    const getAuthBlock = () => {
        if (isAuthStateLoading || isLogoutLoading) {
            return <div data-testid="auth-loader">{t.mainHeader.loading}</div>;
        } else if (username != null) {
            return <div>
                <p>{firstName} {lastName}</p>
                <button id="logoutButton" onClick={handleLogout}>{t.mainHeader.logout}</button>
            </div>;
        } else {
            return <ul>
                <li><NavLink to="/login">{t.mainHeader.login}</NavLink></li>
                <li><NavLink to="/registration">{t.mainHeader.register}</NavLink></li>
            </ul>;
        }
    };

    return (
        <header className={styles.main_header} data-testid="main-header">
            <nav>
                <ul>
                    <li><NavLink to="/">{t.mainHeader.nav.home}</NavLink></li>
                </ul>
            </nav>
            <LanguageHandler/>
            <div className="authBlock">
                {getAuthBlock()}
            </div>
        </header>
    );
};

export default Header;
