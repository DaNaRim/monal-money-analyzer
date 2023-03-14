import React from "react";
import useTranslation from "../../../../app/hooks/translation";
import styles from "./Loading.module.scss";

const Loading = () => {
    const t = useTranslation();

    return (
        <main className={styles.loading}>
            <h2>{t.mainLoader}</h2>
        </main>
    );
};

export default Loading;
