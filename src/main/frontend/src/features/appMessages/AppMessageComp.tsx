import React from "react";
import useTranslation from "../../app/hooks/translation";
import styles from "./AppMessage.module.scss";
import { type AppMessageType } from "./appMessagesSlice";

type Formatted = number | string | JSX.Element; // duplicate from react-localization

interface AppMessageCompProps {
    type: AppMessageType;
    page?: string; // Needed for i18n message code if code belongs to a page
    messageCode: string; // i18n message code
    messageArgs?: Formatted;
    children?: JSX.Element | null; // JSX elements that render after message inside the Component
}

const AppMessageComp = ({
                            messageCode,
                            messageArgs = "",
                            type,
                            page = "",
                            children,
                        }: AppMessageCompProps) => {
    const t = useTranslation();

    const preparedMessageCode = page == null
        ? messageCode
        : `${page}Page.appMessages.${messageCode}`;

    const message = t.formatString(t.getString(preparedMessageCode), messageArgs);

    return (
        <div className={AppMessageCodeClassMap[type]}>
            <p className={styles.message}>{message}</p>
            {children}
        </div>
    );
};

export default AppMessageComp;

const AppMessageCodeClassMap = {
    INFO: `${styles.app_message} ${styles.info}`,
    WARNING: `${styles.app_message} ${styles.warn}`,
    ERROR: `${styles.app_message} ${styles.error}`,
};
