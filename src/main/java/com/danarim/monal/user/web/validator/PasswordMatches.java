package com.danarim.monal.user.web.validator;

import com.danarim.monal.user.web.dto.PasswordEntity;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import javax.validation.Constraint;
import javax.validation.Payload;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Annotation for password matching validation. The annotated element must implement
 * {@link PasswordEntity}.
 */
@Target({TYPE, ANNOTATION_TYPE})
@Retention(RUNTIME)
@Constraint(validatedBy = PasswordMatchesValidator.class)
@Documented
public @interface PasswordMatches {

    /**
     * Error message template.
     *
     * @return the error message template
     */
    String message() default "Passwords don't match";

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
