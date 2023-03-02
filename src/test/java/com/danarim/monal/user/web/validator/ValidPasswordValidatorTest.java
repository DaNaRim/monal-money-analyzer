package com.danarim.monal.user.web.validator;

import com.danarim.monal.config.WebConfig;
import com.danarim.monal.exceptions.InternalServerException;
import com.google.common.collect.Iterables;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.stream.Stream;
import javax.validation.ConstraintValidatorContext;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ValidPasswordValidatorTest {

    private static final int SUPPORTED_LOCALE_COUNT = 1; //WebConfig.SUPPORTED_LOCALES.size()

    private static final LogCaptor logCaptor = LogCaptor.forClass(ValidPasswordValidator.class);

    private static final Iterator<Locale> locales =
            Iterables.cycle(WebConfig.SUPPORTED_LOCALES).iterator();

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

    /*
        Do few checks for each supported locale
        Do few test cases because of iteration over supported locales. In separate tests we can't
         do it correctly.
     */
    @RepeatedTest(SUPPORTED_LOCALE_COUNT)
    void isValid() {
        Locale.setDefault(locales.next());

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

        // long possword
        assertFalse(validator.isValid("1234567890123456789012345678901", context));
        verify(context, times(2))
                .buildConstraintViolationWithTemplate(anyString());

        // white space
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

    @Test
    void isValid_UnsupportedLocale_InternalServerException() {
        Locale.setDefault(Stream.of(DateFormat.getAvailableLocales())
                                  .filter(locale -> !WebConfig.SUPPORTED_LOCALES.contains(locale))
                                  .findFirst()
                                  .orElseThrow());

        assertThrows(InternalServerException.class,
                     () -> validator.isValid("12345678", context));

        assertThat(logCaptor.getErrorLogs()).hasSize(1);
    }

    @Test
    void isValid_IoException_InternalServerException() {
        Locale.setDefault(WebConfig.SUPPORTED_LOCALES.get(0));

        try (MockedConstruction<Properties> ignored
                     = mockConstruction(Properties.class,
                                        (mock, context) -> doThrow(IOException.class)
                                                .when(mock).load(any(InputStreamReader.class)))
        ) {
            assertThrows(InternalServerException.class,
                         () -> validator.isValid("12345678", context));

            assertThat(logCaptor.getErrorLogs()).hasSize(1);
        }
    }

}
