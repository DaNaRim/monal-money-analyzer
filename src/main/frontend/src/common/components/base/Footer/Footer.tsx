import useTranslation from "../../../../app/hooks/translation";
import styles from "./Footer.module.scss";

const Footer = () => {
    const t = useTranslation();

    return (
        <div className={styles.mainFooter}>
            <h2>{t.mainFooter.desc}</h2>
        </div>
    );
};

export default Footer;
