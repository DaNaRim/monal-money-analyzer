import useTranslation from "../../../app/hooks/translation";
import usePageTitle from "../../../app/hooks/usePageTitle";

import styles from "./HomePage.module.scss";

const HomePage = () => {
    usePageTitle();

    const t = useTranslation();

    return (
        <main className={styles.home_page} data-testid="home-page">
            <h1>{t.homePage.title}</h1>
        </main>
    );
};

export default HomePage;
