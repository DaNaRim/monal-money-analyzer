import React from "react";
import useTranslation from "../../../app/hooks/translation";

import styles from "./HomePage.module.scss";

const HomePage = () => {
    const t = useTranslation();

    return (
        <main className={styles.home_page}>
            <h1>{t.homePage.title}</h1>
        </main>
    );
};

export default HomePage;
