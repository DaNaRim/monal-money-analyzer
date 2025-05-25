import { faSignOutAlt } from "@fortawesome/free-solid-svg-icons";
import { FontAwesomeIcon } from "@fortawesome/react-fontawesome";
import React, { useEffect } from "react";
import { useNavigate } from "react-router";
import { NavLink } from "react-router-dom";
import { useAppDispatch, useAppSelector } from "../../../../app/hooks/reduxHooks";
import useTranslation from "../../../../app/hooks/translation";
import {
    ROUTE_ANALYTICS,
    ROUTE_HOME,
    ROUTE_LOGIN,
    ROUTE_REGISTRATION,
    ROUTE_TRANSACTIONS,
} from "../../../../app/routes";

import logo_small from "../../../../assets/img/logo/monal_logo_small.png";
import { apiSlice } from "../../../../features/api/apiSlice";
import { useAuthGetStateMutation, useLogoutMutation } from "../../../../features/auth/authApiSlice";
import {
    clearAuthState,
    Role,
    selectAuthIsInitialized,
    selectAuthRoles,
    selectAuthUsername,
    setCredentials,
    setInitialized,
} from "../../../../features/auth/authSlice";
import LanguageHandler from "../LanguageHandler/LanguageHandler";
import ThemeHandler from "../ThemeHandler/ThemeHandler";

import styles from "./Header.module.scss";

const Header = () => {
    const dispatch = useAppDispatch();
    const navigate = useNavigate();
    const t = useTranslation();

    const username = useAppSelector(selectAuthUsername);

    const isAuthInit = useAppSelector(selectAuthIsInitialized);

    const [getAuthState, { isLoading: isAuthStateLoading }] = useAuthGetStateMutation();
    const [logout, { isLoading: isLogoutLoading }] = useLogoutMutation();

    const handleLogout = () => {
        void logout();
        // Clear redux state
        dispatch(clearAuthState());
        dispatch(apiSlice.util.resetApiState()); // Clear api cache
        navigate(ROUTE_LOGIN);
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
                <p>{username}</p>
                <button id="logoutButton" className={styles.logout_button} onClick={handleLogout}>
                    <FontAwesomeIcon icon={faSignOutAlt}/>
                    {/*{t.mainHeader.logout}*/}
                </button>
            </div>;
        } else {
            return <ul>
                <li><NavLink to={ROUTE_LOGIN}>{t.mainHeader.login}</NavLink></li>
                <li><NavLink to={ROUTE_REGISTRATION}>{t.mainHeader.register}</NavLink></li>
            </ul>;
        }
    };

    return (
        <header className={styles.main_header} data-testid="main-header">
            <nav>
                <div className={styles.logo}>
                    <NavLink to={ROUTE_HOME}>
                        <img src={logo_small} alt="logo" height="64"/>
                        <div className={styles.logo_text}>
                            {t.mainHeader.logo_main_text}<br/>
                            <span>{t.mainHeader.logo_sub_text}</span>
                        </div>
                    </NavLink>
                </div>
                <ul>
                    <PrivateLink to={ROUTE_TRANSACTIONS}>
                        {t.mainHeader.nav.transactions}
                    </PrivateLink>
                    <PrivateLink to={ROUTE_ANALYTICS}>
                        {t.mainHeader.nav.analytics}
                    </PrivateLink>
                </ul>
            </nav>
            <div className={styles.right_block}>
                <LanguageHandler/>
                <ThemeHandler/>
                <div className={styles.auth_block}>
                    {getAuthBlock()}
                </div>
            </div>
        </header>
    );
};

export default Header;

interface PrivateLinkProps {
    role?: Role;
    to: string;
    children: React.ReactNode;
}

const PrivateLink = ({ role = Role.ROLE_USER, to, children }: PrivateLinkProps) => {
    const userRoles = useAppSelector(selectAuthRoles);

    if (userRoles.includes(role)) {
        return <li><NavLink to={to}>{children}</NavLink></li>;
    }
    return <></>;
};
