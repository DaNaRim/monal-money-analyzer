import { type Theme, useThemeContext } from "../../../../app/contexts/ThemeContext";

const ThemeHandler = () => {
    const { theme, changeTheme } = useThemeContext();

    return (
        <select
            value={theme}
            onChange={e => changeTheme(e.target.value as Theme)}
        >
            <option value="dark">Dark</option>
            <option value="light">Light</option>
        </select>
    );
};

export default ThemeHandler;
