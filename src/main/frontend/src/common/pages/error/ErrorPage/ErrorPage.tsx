import React from "react";
import { Link } from "react-router-dom";
import useTranslation from "../../../../app/hooks/translation";
import { ROUTE_HOME } from "../../../../app/routes";
import styles from "./ErrorPage.module.scss";

const ErrorPage = () => {
    const t = useTranslation();

    return (
        <main className={styles.error_page} data-testid="error-page">
            <h1>{t.errorPage.header}</h1>
            <p>{t.errorPage.desc}</p>
            <Link to={ROUTE_HOME}>{t.errorPage.link}</Link>
        </main>
    );
};

export default ErrorPage;
