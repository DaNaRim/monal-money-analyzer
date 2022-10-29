package com.danarim.monal.user.web.validator;

import com.danarim.monal.exceptions.InternalServerException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.passay.*;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Locale;
import java.util.Properties;

import static com.danarim.monal.config.WebConfig.DEFAULT_LOCALE;

@Component
public class ValidPasswordValidator implements ConstraintValidator<ValidPassword, String> {

    private static final String PASSAY_MESSAGE_FILE_PATH = "src/main/resources/i18n/validation%s.properties";

    private static final Log logger = LogFactory.getLog(ValidPasswordValidator.class);

    @Override
    public boolean isValid(String password, ConstraintValidatorContext context) {
        if (password == null) {
            return false;
        }
        PasswordValidator validator = new PasswordValidator(generateMessageResolver(), Arrays.asList(
                new LengthRule(8, 30),
                new WhitespaceRule())
        );
        RuleResult result = validator.validate(new PasswordData(password));

        if (result.isValid()) {
            return true;
        }

        context.buildConstraintViolationWithTemplate(
                        validator.getMessages(result)
                                .stream()
                                .findFirst()
                                .orElseGet(context::getDefaultConstraintMessageTemplate)
                )
                .addConstraintViolation()
                .disableDefaultConstraintViolation();

        return false;
    }

    private static MessageResolver generateMessageResolver() {

        Locale locale = LocaleContextHolder.getLocale();

        String suffix = locale.equals(DEFAULT_LOCALE) ? "" : "_%s".formatted(locale.getLanguage());

        String path = String.format(PASSAY_MESSAGE_FILE_PATH, suffix);

        try (FileInputStream fis = new FileInputStream(path)) {
            Properties props = new Properties();

            props.load(new InputStreamReader(fis, StandardCharsets.UTF_8));
            return new PropertiesMessageResolver(props);
        } catch (IOException e) {
            logger.error("Error while loading Passay messages file", e);
            throw new InternalServerException("Error while loading Passay messages file", e);
        }
    }

}
