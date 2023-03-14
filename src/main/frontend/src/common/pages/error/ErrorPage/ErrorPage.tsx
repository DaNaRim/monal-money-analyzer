import React from "react";
import {Link} from "react-router-dom";
import useTranslation from "../../../../app/hooks/translation";
import styles from "./ErrorPage.module.scss";

const ErrorPage = () => {
    const t = useTranslation();

    return (
        <main className={styles.error_page}>
            <h1>{t.errorPages.main.header}</h1>
            <p>{t.errorPages.main.desc}</p>
            <Link to="/">{t.errorPages.main.link}</Link>
        </main>
    );
};

export default ErrorPage;
