import React from "react";
import {Link} from "react-router-dom";
import useTranslation from "../../../../app/hooks/translation";
import styles from "./NotFoundPage.module.scss";

const NotFoundPage = () => {
    const t = useTranslation();

    return (
        <main className={styles.not_found_page}>
            <h1>{t.errorPages.notFound.header}</h1>
            <p>{t.errorPages.notFound.desc}</p>
            <Link to="/">{t.errorPages.notFound.link}</Link>
        </main>
    );
};

export default NotFoundPage;
