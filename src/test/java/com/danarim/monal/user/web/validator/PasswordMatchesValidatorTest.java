package com.danarim.monal.user.web.validator;

import com.danarim.monal.user.web.dto.RegistrationDto;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PasswordMatchesValidatorTest {

    private static final LogCaptor logCaptor = LogCaptor.forClass(PasswordMatchesValidator.class);

    private final PasswordMatchesValidator validator = new PasswordMatchesValidator();

    @AfterAll
    public static void tearDown() {
        logCaptor.close();
    }

    @AfterEach
    public void clearLogs() {
        logCaptor.clearLogs();
    }

    @Test
    void isValid_RegistrationDto_True() {
        RegistrationDto registrationDto = mock(RegistrationDto.class);

        when(registrationDto.password()).thenReturn("password");
        when(registrationDto.matchingPassword()).thenReturn("password");

        assertTrue(validator.isValid(registrationDto, null));
    }

    @Test
    void isValid_RegistrationDtoDifPasswords_False() {
        RegistrationDto registrationDto = mock(RegistrationDto.class);

        when(registrationDto.password()).thenReturn("password");
        when(registrationDto.matchingPassword()).thenReturn("invalid");

        assertFalse(validator.isValid(registrationDto, null));
    }

    @Test
    void isValid_InvalidClass_InternalServerException() {
        Object invalidClass = new Object();
        assertThrows(RuntimeException.class,
                     () -> validator.isValid(invalidClass, null));
        assertThat(logCaptor.getErrorLogs()).hasSize(1);
    }

}
