package com.danarim.monal.user.web.validator;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Check if the password is valid.
 */
@Documented
@Constraint(validatedBy = ValidPasswordValidator.class)
@Target({TYPE, FIELD, ANNOTATION_TYPE})
@Retention(RUNTIME)
public @interface ValidPassword {

    /**
     * Error message template.
     *
     * @return the error message template
     */
    String message() default "Invalid Password";

    /**
     * The groups the constraint belongs to.
     *
     * @return the groups the constraint belongs to
     */
    Class<?>[] groups() default {};

    /**
     * The payload associated to the constraint.
     *
     * @return the payload associated to the constraint
     */
    Class<? extends Payload>[] payload() default {};

}
