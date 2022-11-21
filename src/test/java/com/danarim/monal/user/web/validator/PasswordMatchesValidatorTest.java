package com.danarim.monal.user.web.validator;

import com.danarim.monal.user.web.dto.RegistrationDto;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class PasswordMatchesValidatorTest {

    private static final MockedStatic<LogFactory> loggerFactoryMock = mockStatic(LogFactory.class);
    private static final Log logger = mock(Log.class);

    private final PasswordMatchesValidator validator = new PasswordMatchesValidator();

    @BeforeAll
    public static void beforeClass() {
        loggerFactoryMock.when(() -> LogFactory.getLog(any(Class.class))).thenReturn(logger);
    }

    @AfterAll
    static void afterAll() {
        loggerFactoryMock.close();
    }

    @Test
    void testWithValidRegistrationDto() {
        RegistrationDto registrationDto = new RegistrationDto(
                "test", "test",
                "password", "password",
                "email"
        );
        assertTrue(validator.isValid(registrationDto, null));
    }

    @Test
    void testWithInvalidRegistrationDto() {
        RegistrationDto regDtoWrongPassword = new RegistrationDto(
                "test", "test",
                "password", "invalid",
                "email"
        );
        assertFalse(validator.isValid(regDtoWrongPassword, null));
    }

    @Test
    void testWithInvalidClass() {
        Object invalidClass = new Object();
        assertThrows(RuntimeException.class, () -> validator.isValid(invalidClass, null));
        verify(logger).error(anyString(), any(ClassCastException.class));
    }
}
