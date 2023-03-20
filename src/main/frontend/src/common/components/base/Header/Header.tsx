import React, {useEffect} from "react";
import {useNavigate} from "react-router";
import {NavLink} from "react-router-dom";
import {useAppDispatch, useAppSelector} from "../../../../app/hooks/reduxHooks";
import useTranslation from "../../../../app/hooks/translation";
import {
    useAuthGetStateMutation,
    useAuthRefreshMutation,
    useLogoutMutation,
} from "../../../../features/auth/authApiSlice";
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

    const [getAuthState, {isLoading: isAuthStateLoading}] = useAuthGetStateMutation();
    const [, {isLoading: isRequestAuthLoading}] = useAuthRefreshMutation();
    const [logout, {isLoading: isLogoutLoading}] = useLogoutMutation();

    const handleLogout = () => {
        logout();
        dispatch(clearAuthState());
        navigate("/login");
    };

    useEffect(() => {
        if (isAuthInit) {
            return;
        }
        const authInitCookie = document.cookie.split("; ").find(row => row.startsWith("authInit="));

        if (authInitCookie) {
            getAuthState().unwrap()
                .then(res => dispatch(setCredentials(res)))
                .catch(() => dispatch(setInitialized()));
        } else {
            dispatch(setInitialized());
        }
    }, [dispatch, getAuthState, isAuthInit]);

    const getAuthBlock = () => {
        if (isAuthStateLoading || isRequestAuthLoading || isLogoutLoading) {
            return <div>{t.mainHeader.loading}</div>;
        } else if (username) {
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
        <header className={styles.main_header}>
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
