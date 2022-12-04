import React, {useEffect} from "react";
import {useNavigate} from "react-router";
import {NavLink} from "react-router-dom";
import {useAppDispatch, useAppSelector} from "../../../../app/hooks";
import {useGetAuthStateMutation, useLogoutMutation} from "../../../../features/auth/authApiSlice";
import {
    clearAuthState,
    selectAuthFirstname,
    selectAuthInitialized,
    selectAuthLastname,
    selectAuthUsername,
    setCredentials,
    setInitialized,
} from "../../../../features/auth/authSlice";

import styles from "./Header.module.scss";

const Header = () => {
    const dispatch = useAppDispatch();
    const navigate = useNavigate();

    const username = useAppSelector(selectAuthUsername);
    const firstName = useAppSelector(selectAuthFirstname);
    const lastName = useAppSelector(selectAuthLastname);

    const isAuthInit = useAppSelector(selectAuthInitialized);

    const [getAuthState, {isLoading: isAuthStateLoading}] = useGetAuthStateMutation();
    const [logout, {isLoading: isLogoutLoading}] = useLogoutMutation();

    const handleLogout = () => {
        logout();
        dispatch(clearAuthState());
        navigate("/login");
    };

    useEffect(() => {
        if (!isAuthInit) {
            getAuthState().unwrap()
                .then(res => dispatch(setCredentials(res)))
                .catch(() => {
                    logout();
                    dispatch(clearAuthState());
                    dispatch(setInitialized());
                });
        }
    }, []);

    const getAuthBlock = () => {
        if (isAuthStateLoading || isLogoutLoading) {
            return <div>Loading...</div>;
        } else if (username) {
            return <div>
                <p>{firstName} {lastName}</p>
                <button id="logoutButton" onClick={handleLogout}>Logout</button>
            </div>;
        } else {
            return <ul>
                <li><NavLink to="/login">Login</NavLink></li>
                <li><NavLink to="/registration">Registration</NavLink></li>
            </ul>;
        }
    };

    return (
        <header className={styles.main_header}>
            <nav>
                <ul>
                    <li><NavLink to="/">Home</NavLink></li>
                </ul>
            </nav>
            <div className="logoBlock">

            </div>
            <div className="authBlock">
                {getAuthBlock()}
            </div>
        </header>
    );
};

export default Header;
