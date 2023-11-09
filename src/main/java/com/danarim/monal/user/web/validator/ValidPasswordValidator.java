package com.danarim.monal.user.web.validator;

import org.passay.LengthRule;
import org.passay.PasswordData;
import org.passay.PasswordValidator;
import org.passay.Rule;
import org.passay.RuleResult;
import org.passay.WhitespaceRule;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Checks if the password is valid by passwordRules.
 */
@Component
public class ValidPasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private static final List<Rule> passwordRules = Arrays.asList(
            new LengthRule(8, 30),
            new WhitespaceRule()
    );

    /**
     * Checks if the password is valid by passwordRules. If not, add error message to the context.
     *
     * @param password password to validate
     * @param context  context in which the constraint is evaluated
     *
     * @return true if the password is valid, false otherwise
     */
    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return false;
        }
        PasswordValidator validator = new PasswordValidator(passwordRules);
        RuleResult result = validator.validate(new PasswordData(password));

        if (result.isValid()) {
            return true;
        }
        context.buildConstraintViolationWithTemplate(
                        result.getDetails().get(0).toString()
                )
                .addConstraintViolation()
                .disableDefaultConstraintViolation();

        return false;
    }

}
