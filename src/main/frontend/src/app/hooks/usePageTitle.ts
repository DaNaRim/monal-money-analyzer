import { useEffect } from "react";
import useTranslation from "./translation";

/**
 * Updates the page title based on the provided page name. If no page name is
 * provided, the app name is used. The Title is updated when the page name changes or when
 * language changes.
 *
 * In i18n page key must contain a `seo_title` key, which is used as the page title.
 * If the key is not found, the app name is used.
 *
 * @example
 * usePageTitle("loginPage");
 * // Sets the page title to "Login - [app name]"
 * @example
 * usePageTitle();
 * // Sets the page title to "[app name]"
 *
 * @param pageName The name of the current page. Must be a valid translation key.
 */
const usePageTitle = (pageName?: string): void => {
    const t = useTranslation();

    useEffect(() => {
        if (pageName === undefined) {
            document.title = t.appName;
            return;
        }
        const title = t.getString(`${pageName}.seo_title`);

        if (title === undefined) {
            document.title = t.appName;
            return;
        }
        document.title = t.formatString(title, t.appName) as string;
    }, [t, pageName]);
};

export default usePageTitle;
