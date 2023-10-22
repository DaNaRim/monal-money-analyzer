import { createContext, ReactNode, useContext, useEffect, useMemo } from "react";
import useLocalStorage from "react-use-localstorage";

const LOCAL_STORAGE_THEME = "theme";

export enum Theme {
    DARK = "dark",
    LIGHT = "light",
}

interface ContextType {
    theme: Theme;
    changeTheme: (theme: Theme) => void;
}

const ThemeContext = createContext({
    theme: Theme.DARK,
    changeTheme: (theme: Theme) => {
    },
});

export const useThemeContext: () => ContextType = () => useContext(ThemeContext);

const getTheme = () => {
    const userMedia = window.matchMedia("(prefers-color-scheme: light)");
    if (userMedia.matches) {
        return Theme.LIGHT;
    }
    return Theme.DARK;
};

const ThemeContextProvider = ({ children }: { children: ReactNode }) => {
    const [theme, setTheme] = useLocalStorage(LOCAL_STORAGE_THEME, getTheme());

    useEffect(() => {
        document.documentElement.dataset.theme = theme;
        setTheme(theme);
    }, [theme]);

    const changeTheme = (theme: Theme): void => {
        setTheme(theme);
    };

    const providerValue = useMemo(() => ({ theme: theme as Theme, changeTheme }),
        [theme, setTheme]);

    return (
        <ThemeContext.Provider value={providerValue}>
            {children}
        </ThemeContext.Provider>
    );
};

export default ThemeContextProvider;
