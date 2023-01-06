import React from "react";
import PageWrapper from "../../components/pageComponents/PageWrapper/PageWrapper";

import styles from "./HomePage.module.scss";

const HomePage = () => {
    return (
        <PageWrapper>
            <main className={styles.home_page}>
                <h1>Home Page</h1>
            </main>
        </PageWrapper>
    );
};

export default HomePage;
