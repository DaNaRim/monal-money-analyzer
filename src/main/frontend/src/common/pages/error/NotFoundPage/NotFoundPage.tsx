import React from "react";
import {Link} from "react-router-dom";
import styles from "./NotFoundPage.module.scss";

const NotFoundPage = () => (
    <main className={styles.not_found_page}>
        <h1>Page not found</h1>
        <Link to="/">Go to home page</Link>
    </main>
);

export default NotFoundPage;
