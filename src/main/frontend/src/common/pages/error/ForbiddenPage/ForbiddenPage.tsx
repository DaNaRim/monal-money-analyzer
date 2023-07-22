import React from "react";
import { Link } from "react-router-dom";
import useTranslation from "../../../../app/hooks/translation";
import styles from "./ForbiddenPage.module.scss";

const ForbiddenPage = () => {
    const t = useTranslation();

    return (
        <main className={styles.forbidden_page} data-testid="forbidden-page">
            <h1>{t.forbiddenPage.header}</h1>
            <p>{t.forbiddenPage.desc}</p>
            <Link to="/">{t.forbiddenPage.link}</Link>
        </main>
    );
};

export default ForbiddenPage;
