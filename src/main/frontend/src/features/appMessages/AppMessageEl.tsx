import useTranslation from "../../app/hooks/translation";
import styles from "./AppMessage.module.scss";
import {AppMessage, AppMessageType} from "./appMessagesSlice";

type Formatted = number | string | JSX.Element; //duplicate from react-localization

type AppMessageElProps = AppMessage & {
    messageArgs?: Formatted;
    children?: JSX.Element | null;
}

const AppMessageEl = (props: AppMessageElProps) => {
    const t = useTranslation();

    const preparedMessageCode = props.messageCode.replace(/[.\-]/g, "_");

    const message = props.messageArgs
        ? t.formatString(`appMessages.${preparedMessageCode}`, preparedMessageCode)
        : t.getString(`appMessages.${preparedMessageCode}`);

    return (
        <div className={getAppMessageClassName(props.type)}>
            <p className={styles.message}>{message}</p>
            {props.children}
        </div>
    );
};

export default AppMessageEl;

const getAppMessageClassName = (type: AppMessageType) => {
    const classMap = {
        "INFO": `${styles.app_message} ${styles.info}`,
        "WARNING": `${styles.app_message} ${styles.warn}`,
        "ERROR": `${styles.app_message} ${styles.error}`,
    };
    return classMap[type];
};
