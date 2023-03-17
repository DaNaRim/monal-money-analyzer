import React, {useContext, useMemo, useState} from "react";

const COOKIE_LOCALE_KEY = "locale";

type ContextType = {
    language: string;
    changeLanguage: (language: string) => void;
}

const supportedLanguages = [
    "en",
    "uk",
];

const localeFromCookie = document.cookie.split("; ")
        .find(row => row.startsWith(`${COOKIE_LOCALE_KEY}=`))
        ?.split("=")[1]
    || "en";

const LanguageContext = React.createContext<ContextType>({
    language: localeFromCookie, changeLanguage: () => {},
});

const updateLocaleCookie = (locale: string) => {
    document.cookie = `${COOKIE_LOCALE_KEY}=${locale}; path=/; secure;`;
}

export const useLanguageContext = () => useContext(LanguageContext);

const LanguageContextProvider = ({children}: { children: React.ReactNode }) => {
    const [language, setLanguage] = useState(localeFromCookie);

    const changeLanguage = (language: string) => {
        if (supportedLanguages.includes(language)) {
            setLanguage(language);
            updateLocaleCookie(language);
        }
    };
    const providerValue = useMemo(() => ({language, changeLanguage}), [language, changeLanguage]);

    return (
        <LanguageContext.Provider value={providerValue}>
            {children}
        </LanguageContext.Provider>
    );
};

export default LanguageContextProvider;