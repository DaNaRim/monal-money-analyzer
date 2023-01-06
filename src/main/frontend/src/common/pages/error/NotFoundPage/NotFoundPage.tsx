import React from "react";
import {Link} from "react-router-dom";
import PageWrapper from "../../../components/pageComponents/PageWrapper/PageWrapper";
import styles from "./NotFoundPage.module.scss";

const NotFoundPage = () => {
    return (
        <PageWrapper>
            <main className={styles.not_found_page}>
                <h1>Page not found</h1>
                <Link to="/">Go to home page</Link>
            </main>
        </PageWrapper>
    );
};

export default NotFoundPage;
