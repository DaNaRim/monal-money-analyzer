package com.danarim.monal.user.web.validator;

import com.danarim.monal.config.WebConfig;
import com.danarim.monal.exceptions.InternalServerException;
import com.google.common.collect.Iterables;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import javax.validation.ConstraintValidatorContext;
import java.text.DateFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ValidPasswordValidatorTest {

    private static final MockedStatic<LogFactory> loggerFactoryMock = mockStatic(LogFactory.class);
    private static final Log logger = mock(Log.class);

    private static final Iterator<Locale> locales = Iterables.cycle(WebConfig.SUPPORTED_LOCALES).iterator();

    private final ValidPasswordValidator validator = new ValidPasswordValidator();

    private final ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

    @BeforeAll
    public static void beforeClass() {
        loggerFactoryMock.when(() -> LogFactory.getLog(any(Class.class))).thenReturn(logger);
    }

    @AfterAll
    static void afterAll() {
        loggerFactoryMock.close();
    }

    /**
     * RepeatedTest param value is SUPPORTED_LOCALES size
     */
    @RepeatedTest(3)
    void testIsValid() {
        Locale.setDefault(locales.next());

        ConstraintValidatorContext.ConstraintViolationBuilder builder
                = mock(ConstraintValidatorContext.ConstraintViolationBuilder.class);

        when(context.buildConstraintViolationWithTemplate(anyString())).thenReturn(builder);
        when(builder.addConstraintViolation()).thenReturn(context);

        assertTrue(validator.isValid("12345678", context));
        assertTrue(validator.isValid("ASDEFGHI", context));
        assertTrue(validator.isValid("12345678ASDEFGHI", context));

        assertFalse(validator.isValid("1234567", context));
        verify(context, times(1)).buildConstraintViolationWithTemplate(anyString());

        assertFalse(validator.isValid("1234567890123456789012345678901", context));
        verify(context, times(2)).buildConstraintViolationWithTemplate(anyString());

        assertFalse(validator.isValid("12345678 ", context));
        verify(context, times(3)).buildConstraintViolationWithTemplate(anyString());

        assertFalse(validator.isValid("  ", context));
        verify(context, times(4)).buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    void testIsValidIOException() {
        Locale.setDefault(Stream.of(DateFormat.getAvailableLocales())
                .filter(locale -> !WebConfig.SUPPORTED_LOCALES.contains(locale))
                .findFirst()
                .orElseThrow());

        assertThrows(InternalServerException.class, () -> validator.isValid("12345678", context));

        verify(logger, times(1)).error(anyString(), any(Exception.class));
    }
}
