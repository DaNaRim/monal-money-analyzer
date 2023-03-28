import LocalizedStrings from "react-localization";
import localization from "../../i18n";
import { useLanguageContext } from "../contexts/LanguageContext";

function useTranslation() {
    const { language } = useLanguageContext();
    const translation = new LocalizedStrings(localization);

    translation.setLanguage(language);
    return translation;
}

export default useTranslation;
