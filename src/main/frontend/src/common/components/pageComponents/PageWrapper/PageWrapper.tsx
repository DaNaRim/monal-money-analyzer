import React, {Suspense} from "react";
import {Outlet} from "react-router";
import Footer from "../Footer/Footer";
import Header from "../Header/Header";
import Loading from "../Loading/Loading";
import styles from "./PageWrapper.module.scss";

const PageWrapper = () => (
    <div className={styles.pageWrapper}>
        <Header/>
        <Suspense fallback={<Loading/>}>
            <Outlet/>
        </Suspense>
        <Footer/>
    </div>
);

export default PageWrapper;
