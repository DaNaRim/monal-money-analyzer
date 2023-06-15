import React from "react";
import useTranslation from "../../../../app/hooks/translation";
import styles from "./Footer.module.scss";

const Footer = () => {
    const t = useTranslation();

    return (
        <div className={styles.main_footer} data-testid="main-footer">
            <h2>{t.mainFooter.desc}</h2>
        </footer>
    );
};

export default Footer;
