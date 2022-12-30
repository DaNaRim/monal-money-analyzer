import PageWrapper from "@components/pageComponents/PageWrapper/PageWrapper";
import React from "react";
import {Link} from "react-router-dom";
import styles from "./ErrorPage.module.scss";

const ErrorPage = () => {
    return (
        <PageWrapper>
            <main className={styles.error_page}>
                <h1>Error Page</h1>
                <p>Something went wrong. Please try again later.</p>
                <Link to={"/"}>Go to home page</Link>
            </main>
        </PageWrapper>
    );
};

export default ErrorPage;
