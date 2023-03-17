import {ErrorProps} from "../ErrorGlobal/ErrorGlobal";
import styles from "./ErrorServer.module.scss";

const ErrorServer = ({register, errors}: ErrorProps) => (
    <>
        <input type="hidden" {...register("serverError")}/>
        {errors.serverError && <span className={styles.message}>{errors.serverError.message as string}</span>}
    </>
);

export default ErrorServer;
