import React from "react";
import {Link} from "react-router-dom";
import PageWrapper from "../../../components/pageComponents/PageWrapper/PageWrapper";
import styles from "./ForbiddenPage.module.scss";

const ForbiddenPage = () => {
    return (
        <PageWrapper>
            <main className={styles.forbidden_page}>
                <h1>Forbidden Page</h1>
                <p>You don't have permission to access this page.</p>
                <Link to={"/"}>Go to home page</Link>
            </main>
        </PageWrapper>
    );
};

export default ForbiddenPage;