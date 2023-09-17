import LocalizedStrings, {
    type LocalizedStrings as LocalizedStringsType,
} from "react-localization";
import localization, { type Localization } from "../../i18n";
import { useLanguageContext } from "../contexts/LanguageContext";

/**
 * Hook for getting the translation object.
 *
 * <p>To typify the translations, use this: (t: LocalizedStrings<Localization>)
 * from {@link Localization i18n/index.ts}
 *
 * @returns LocalizedStrings The translation object.
 */
function useTranslation(): LocalizedStringsType<Localization> {
    const { language } = useLanguageContext();
    const translation = new LocalizedStrings(localization);

    translation.setLanguage(language);
    return translation;
}

export default useTranslation;
