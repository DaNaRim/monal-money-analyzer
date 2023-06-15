import React, { useContext, useMemo, useState } from "react";

const COOKIE_LOCALE_KEY = "locale";

interface ContextType {
    language: string;
    changeLanguage: (language: string) => void;
}

const supportedLanguages = [
    "en",
    "uk",
];

const defaultLocale = "en";

const localeFromCookie = document.cookie.split("; ")
        .find(row => row.startsWith(`${COOKIE_LOCALE_KEY}=`))?.split("=")[1]
    ?? defaultLocale;

const LanguageContext = React.createContext<ContextType>({
    language: localeFromCookie,
    changeLanguage: () => {
    },
});

const updateLocaleCookie = (locale: string): void => {
    document.cookie = `${COOKIE_LOCALE_KEY}=${locale}; path=/; secure;`;
};

const initLocaleCookie = (): void => {
    if (document.cookie.split("; ")
        .find(row => row.startsWith(`${COOKIE_LOCALE_KEY}=`)) === undefined) {
        updateLocaleCookie(defaultLocale);
    }
};

export const useLanguageContext: () => ContextType = () => useContext(LanguageContext);

const LanguageContextProvider = ({ children }: { children: React.ReactNode }): JSX.Element => {
    const [language, setLanguage] = useState(localeFromCookie);

    initLocaleCookie();

    const changeLanguage = (language: string): void => {
        if (supportedLanguages.includes(language)) {
            setLanguage(language);
            updateLocaleCookie(language);
        }
    };
    const providerValue = useMemo(() => ({ language, changeLanguage }), [language, changeLanguage]);

    return (
        <LanguageContext.Provider value={providerValue}>
            {children}
        </LanguageContext.Provider>
    );
};

export default LanguageContextProvider;
