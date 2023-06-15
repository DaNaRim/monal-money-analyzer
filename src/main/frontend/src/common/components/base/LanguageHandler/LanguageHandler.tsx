import React from "react";
import { useLanguageContext } from "../../../../app/contexts/LanguageContext";

const LanguageHandler = () => {
    const { language, changeLanguage } = useLanguageContext();

    return (
        <select value={language}
                onChange={e => changeLanguage(e.target.value)}
                data-testid="language-handler">
            <option value="en">English</option>
            <option value="uk">Українська</option>
        </select>
    );
};

export default LanguageHandler;
