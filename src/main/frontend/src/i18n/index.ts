import en from "./en";
import uk from "./uk";

// Use this to typify the translations (t: LocalizedStrings<Localization>)
export type Localization = typeof en | typeof uk;

export default {
    en,
    uk,
};
