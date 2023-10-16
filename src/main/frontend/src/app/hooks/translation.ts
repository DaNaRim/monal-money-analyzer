import LocalizedStrings, {
    type LocalizedStrings as LocalizedStringsType,
} from "react-localization";
import localization, { type Localization } from "../../i18n";
import { useLanguageContext } from "../contexts/LanguageContext";

/**
 * Hook for getting the translation object.
 *
 * <p>Example usage:
 * <pre>
 *     const t = useTranslation();
 *     return <div>{t.hello}</div>;
 *
 *     // If the language is set to "en", this will render:
 *     // <div>Hello</div>
 *     // If the language is set to "uk", this will render:
 *     // <div>Привіт</div>
 *     // Don't forget to add the translations to the localization object (i18n/index.ts).
 *
 * <p>To typify the translations, use this: (t: LocalizedStrings<Localization>)
 * from {@link Localization i18n/index.ts}
 *
 * @returns LocalizedStrings The translation object.
 */
function useTranslation(): LocalizedStringsType<Localization> {
    const { language } = useLanguageContext();
    const translation = new LocalizedStrings(localization);

    if (language != null) {
        translation.setLanguage(language);
    }
    return translation;
}

export default useTranslation;
