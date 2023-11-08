package com.danarim.monal.user.web.validator;

import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.validation.ConstraintValidatorContext;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ValidPasswordValidatorTest {

    private static final LogCaptor logCaptor = LogCaptor.forClass(ValidPasswordValidator.class);

    private final ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);
    private final ConstraintValidatorContext.ConstraintViolationBuilder builder
            = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);

    private final ValidPasswordValidator validator = new ValidPasswordValidator();

    @AfterAll
    public static void tearDown() {
        logCaptor.close();
    }

    @AfterEach
    public void clearLogs() {
        logCaptor.clearLogs();
    }

    @Test
    void isValid() {
        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        when(builder.addConstraintViolation()).thenReturn(context);

        // normal
        assertTrue(validator.isValid("12345678", context));
        assertTrue(validator.isValid("ASDEFGHI", context));
        assertTrue(validator.isValid("12345678ASDEFGHI", context));
        assertTrue(validator.isValid("asd#$!@#adsf", context));

        // short possword
        assertFalse(validator.isValid("1234567", context));
        verify(context, times(1))
                .buildConstraintViolationWithTemplate(anyString());

        // long password
        assertFalse(validator.isValid("1234567890123456789012345678901", context));
        verify(context, times(2))
                .buildConstraintViolationWithTemplate(anyString());

        // space
        assertFalse(validator.isValid("12345678 ", context));
        verify(context, times(3))
                .buildConstraintViolationWithTemplate(anyString());

        // blank password
        assertFalse(validator.isValid("  ", context));
        verify(context, times(4))
                .buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    void isValid_NullPassword_False() {
        assertFalse(validator.isValid(null, context));
    }

}
