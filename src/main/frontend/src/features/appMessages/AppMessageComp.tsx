import React from "react";
import useTranslation from "../../app/hooks/translation";
import styles from "./AppMessage.module.scss";
import { type AppMessage, type AppMessageType } from "./appMessagesSlice";

type Formatted = number | string | JSX.Element; // duplicate from react-localization

type AppMessageCompProps = AppMessage & {
    messageArgs?: Formatted;
    children?: JSX.Element | null;
};

const AppMessageComp = (props: AppMessageCompProps) => {
    const t = useTranslation();

    const preparedMessageCode = props.messageCode.replace(/[.-]/g, "_");

    const message = props.messageArgs === undefined
        ? t.getString(`appMessages.${preparedMessageCode}`)
        : t.formatString(`appMessages.${preparedMessageCode}`, preparedMessageCode);

    return (
        <div className={getAppMessageClassName(props.type)}>
            <p className={styles.message}>{message}</p>
            {props.children}
        </div>
    );
};

export default AppMessageComp;

const getAppMessageClassName = (type: AppMessageType) => {
    const classMap = {
        INFO: `${styles.app_message} ${styles.info}`,
        WARNING: `${styles.app_message} ${styles.warn}`,
        ERROR: `${styles.app_message} ${styles.error}`,
    };
    return classMap[type];
};