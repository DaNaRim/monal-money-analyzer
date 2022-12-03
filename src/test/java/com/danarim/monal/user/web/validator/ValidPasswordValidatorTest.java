package com.danarim.monal.user.web.validator;

import com.danarim.monal.config.WebConfig;
import com.danarim.monal.exceptions.InternalServerException;
import com.google.common.collect.Iterables;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.*;
import org.mockito.MockedConstruction;
import org.mockito.MockedStatic;

import javax.validation.ConstraintValidatorContext;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.Properties;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class ValidPasswordValidatorTest {

    private static final int SUPPORTED_LOCALE_COUNT = 1; //WebConfig.SUPPORTED_LOCALES.size()

    private static final MockedStatic<LogFactory> loggerFactoryMock = mockStatic(LogFactory.class);
    private static final Log logger = mock(Log.class);

    private static final Iterator<Locale> locales = Iterables.cycle(WebConfig.SUPPORTED_LOCALES).iterator();

    private final ConstraintValidatorContext context = mock(ConstraintValidatorContext.class);

    private final ValidPasswordValidator validator = new ValidPasswordValidator();

    @BeforeAll
    public static void beforeClass() {
        loggerFactoryMock.when(() -> LogFactory.getLog(any(Class.class))).thenReturn(logger);
    }

    @AfterAll
    static void afterAll() {
        loggerFactoryMock.close();
    }

    @BeforeEach
    void setUp() {
        reset(logger);
    }

    @RepeatedTest(SUPPORTED_LOCALE_COUNT)
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
    void testIsValidNullPassword() {
        assertFalse(validator.isValid(null, context));
    }

    @Test
    void testIsValidMessagesFileNotFound() {
        Locale.setDefault(Stream.of(DateFormat.getAvailableLocales())
                .filter(locale -> !WebConfig.SUPPORTED_LOCALES.contains(locale))
                .findFirst()
                .orElseThrow());

        assertThrows(InternalServerException.class, () -> validator.isValid("12345678", context));

        verify(logger, times(1)).error(anyString());
    }

    @Test
    void testIsValidIOException() {
        Locale.setDefault(WebConfig.SUPPORTED_LOCALES.get(0));

        try (MockedConstruction<Properties> ignored = mockConstruction(Properties.class,
                (mock, context) -> doThrow(IOException.class).when(mock).load(any(InputStreamReader.class)))) {
            assertThrows(InternalServerException.class, () -> validator.isValid("12345678", context));

            verify(logger, times(1)).error(anyString());
        }
    }
}
