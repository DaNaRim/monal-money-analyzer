import { Link } from "react-router-dom";
import useTranslation from "../../../../app/hooks/translation";
import usePageTitle from "../../../../app/hooks/usePageTitle";
import { ROUTE_HOME } from "../../../../app/routes";
import styles from "./ForbiddenPage.module.scss";

const ForbiddenPage = () => {
    usePageTitle("forbiddenPage");

    const t = useTranslation();

    return (
        <main className={styles.forbidden_page} data-testid="forbidden-page">
            <h1>{t.forbiddenPage.header}</h1>
            <p>{t.forbiddenPage.desc}</p>
            <Link to={ROUTE_HOME}>{t.forbiddenPage.link}</Link>
        </main>
    );
};

export default ForbiddenPage;
