import { Link } from "react-router-dom";
import useTranslation from "../../../../app/hooks/translation";
import usePageTitle from "../../../../app/hooks/usePageTitle";
import { ROUTE_HOME } from "../../../../app/routes";
import styles from "./NotFoundPage.module.scss";

const NotFoundPage = () => {
    usePageTitle("notFoundPage");

    const t = useTranslation();

    return (
        <main className={styles.not_found_page} data-testid="not-found-page">
            <h1>{t.notFoundPage.header}</h1>
            <p>{t.notFoundPage.desc}</p>
            <Link to={ROUTE_HOME}>{t.notFoundPage.link}</Link>
        </main>
    );
};

export default NotFoundPage;
