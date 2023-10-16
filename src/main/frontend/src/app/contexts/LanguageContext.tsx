import { createContext, type ReactNode, useContext, useEffect, useMemo, useState } from "react";
import LocalizedStrings from "react-localization";
import localization from "../../i18n";

const COOKIE_LOCALE_KEY = "locale";

interface ContextType {
    /**
     * language - current language. If undefined, then initializing required.
     * Initializing is done inside useEffect in {@link LanguageContextProvider}.
     */
    language: string | undefined;
    changeLanguage: (language: string) => void;
}

export const DEFAULT_LOCALE = "en";

const getLocaleFromCookie = (): string | undefined =>
    document.cookie.split("; ").find(row => row.startsWith(`${COOKIE_LOCALE_KEY}=`))?.split("=")[1];

const LanguageContext = createContext<ContextType>({
    language: getLocaleFromCookie(),
    changeLanguage: () => {
    },
});

const updateLocaleCookie = (locale: string): void => {
    document.cookie = `${COOKIE_LOCALE_KEY}=${locale}; path=/; secure;`;
};

export const useLanguageContext: () => ContextType = () => useContext(LanguageContext);

/**
 * Provider for {@link LanguageContext}.
 * When initialized, it tries to get locale from cookie. If it is not set, then it uses undefined.
 * Further initialization is done inside useEffect.
 * When language is changed, it updates cookie.
 *
 * @param children - children to render.
 */
const LanguageContextProvider = ({ children }: { children: ReactNode }): JSX.Element => {
    const [language, setLanguage] = useState(getLocaleFromCookie());

    const localStrings = new LocalizedStrings(localization);

    const changeLanguage = (language: string): void => {
        if (localStrings.getAvailableLanguages().includes(language)) {
            setLanguage(language);
            updateLocaleCookie(language);
        }
    };
    const providerValue = useMemo(() => ({ language, changeLanguage }), [language, changeLanguage]);

    useEffect(() => {
        // If the language is set (cookie), use it.
        if (language != null) {
            return;
        }
        // If the language is not set, try to set it to the interface language.
        const interfaceLanguage = localStrings.getInterfaceLanguage();
        for (const lang of localStrings.getAvailableLanguages()) {
            if (interfaceLanguage.startsWith(lang)) {
                changeLanguage(lang);
                return;
            }
        }
        // If the interface language is not supported, set the language to the default.
        changeLanguage(DEFAULT_LOCALE);
    }, []);

    return (
        <LanguageContext.Provider value={providerValue}>
            {children}
        </LanguageContext.Provider>
    );
};

export default LanguageContextProvider;
