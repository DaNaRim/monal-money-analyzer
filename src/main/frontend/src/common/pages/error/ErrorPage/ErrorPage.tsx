import React from "react";
import {Link} from "react-router-dom";
import styles from "./ErrorPage.module.scss";

const ErrorPage = () => (
    <main className={styles.error_page}>
        <h1>Error Page</h1>
        <p>Something went wrong. Please try again later.</p>
        <Link to={"/"}>Go to home page</Link>
    </main>
);

export default ErrorPage;
