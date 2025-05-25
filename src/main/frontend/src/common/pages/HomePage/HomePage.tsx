import React from "react";
import { Link } from "react-router-dom";
import useTranslation from "../../../app/hooks/translation";
import usePageTitle from "../../../app/hooks/usePageTitle";
import { ROUTE_LOGIN, ROUTE_REGISTRATION } from "../../../app/routes";

import circle_daily_graph from "../../../assets/img/demo/circle_daily_graph.png";

import styles from "./HomePage.module.scss";

const HomePage = () => {
    usePageTitle();

    const t = useTranslation();

    return (
        <main className={styles.home_page} data-testid="home-page">
            <h1>
                <span className={styles.title_highlight}>{t.homePage.title_part_1}</span>
                <br/>
                {t.homePage.title_part_2}
                <span className={styles.title_highlight}>{t.homePage.title_part_3}</span>
            </h1>
            <section className={styles.join_buttons}>
                <Link to={ROUTE_REGISTRATION}>{t.homePage.button_register}</Link>
                <Link to={ROUTE_LOGIN}>{t.homePage.button_login}</Link>
            </section>
            <section className={styles.demo_images}>
                <img src={circle_daily_graph} alt="circle daily graph" height="64"/>
            </section>
        </main>
    );
};

export default HomePage;
