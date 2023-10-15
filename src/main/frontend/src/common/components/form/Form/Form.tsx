import { type FormEventHandler, type ReactNode, useMemo } from "react";
import { type FieldErrors } from "react-hook-form/dist/types/errors";
import { type UseFormRegister } from "react-hook-form/dist/types/form";
import useTranslation from "../../../../app/hooks/translation";
import ErrorGlobal from "../ErrorGlobal/ErrorGlobal";
import ErrorServer from "../ErrorServer/ErrorServer";
import styles from "./Form.module.scss";

interface FormProps {
    className?: string;
    onSubmit: FormEventHandler<HTMLFormElement>;
    children: ReactNode;
    componentName: string;
    isSubmitting: boolean;

    register: UseFormRegister<any>;
    errors: FieldErrors;
}

/**
 * Component for form wrapper. Adds ErrorGlobal, ErrorServer, and loader.
 * Fields and submit button should be passed as children. You can also pass other ReactNodes.
 *
 * Shows global error and server error if that occurred.
 * Shows loader if form is submitting. Otherwise, shows children.
 *
 * @param className - class name for form
 * @param onSubmit - form submit handler
 * @param children - form children
 * @param componentName - component name for translation
 * @param isSubmitting - is form submitting
 * @param register - react-hook-form register
 * @param errors - react-hook-form errors
 */
const Form = ({
                  className = "",
                  onSubmit,
                  children,
                  componentName,
                  isSubmitting,
                  register,
                  errors,
              }: FormProps) => {
    const t = useTranslation();

    const loader = useMemo(() =>
            t.getString(`${componentName}.form.loading`, t.getLanguage(), true)
            ?? t.getString(`${componentName}.loading`, t.getLanguage(), true)
            ?? t.mainLoader,
        [t, componentName]);

    return (
        <form className={className} onSubmit={onSubmit}>
            {isSubmitting && <p className={styles.loader}>{loader}</p>}
            {!isSubmitting &&
              <>
                  {children}
                <ErrorGlobal {...{ register, errors }}/>
                <ErrorServer {...{ register, errors }}/>
              </>
            }
        </form>
    );
};

export default Form;
