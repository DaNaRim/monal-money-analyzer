import React from "react";
import Footer from "../Footer/Footer";
import Header from "../Header/Header";
import styles from "./PageWrapper.module.scss";

const PageWrapper = (props: any) => {
    return (
        <div className={styles.pageWrapper}>
            <Header/>
            {props.children}
            <Footer/>
        </div>
    );
};

export default PageWrapper;
