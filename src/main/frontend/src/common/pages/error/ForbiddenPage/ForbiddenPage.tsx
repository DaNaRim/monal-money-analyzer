import React from "react";
import { Link } from "react-router-dom";
import useTranslation from "../../../../app/hooks/translation";
import styles from "./ForbiddenPage.module.scss";

const ForbiddenPage = () => {
    const t = useTranslation();

    return (
        <main className={styles.forbiddenPage}>
            <h1>{t.errorPages.forbidden.header}</h1>
            <p>{t.errorPages.forbidden.desc}</p>
            <Link to="/">{t.errorPages.forbidden.link}</Link>
        </main>
    );
};

export default ForbiddenPage;
