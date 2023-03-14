import React, {useContext, useMemo, useState} from "react";

type ContextType = {
    language: string;
    changeLanguage: (language: string) => void;
}

const supportedLanguages = [
    "en",
    "uk",
];

const LanguageContext = React.createContext<ContextType>(null as any);
export const useLanguageContext = () => useContext(LanguageContext);

const LanguageContextProvider = ({children}: { children: React.ReactNode }) => {
    const [language, setLanguage] = useState("en");

    const changeLanguage = (language: string) => {
        if (supportedLanguages.includes(language)) {
            setLanguage(language);
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
