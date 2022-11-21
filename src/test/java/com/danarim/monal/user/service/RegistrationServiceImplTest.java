package com.danarim.monal.user.service;

import com.danarim.monal.exceptions.AlreadyExistsException;
import com.danarim.monal.exceptions.GenericErrorType;
import com.danarim.monal.user.persistence.dao.RoleDao;
import com.danarim.monal.user.persistence.dao.UserDao;
import com.danarim.monal.user.persistence.model.RoleName;
import com.danarim.monal.user.persistence.model.User;
import com.danarim.monal.user.web.dto.RegistrationDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class RegistrationServiceImplTest {

    private final UserDao userDao = mock(UserDao.class);
    private final RoleDao roleDao = mock(RoleDao.class);
    private final PasswordEncoder passwordEncoder = mock(PasswordEncoder.class);

    private final RegistrationServiceImpl registrationService = new RegistrationServiceImpl(
            userDao,
            roleDao,
            passwordEncoder
    );

    @BeforeEach
    void setUp() {
        when(passwordEncoder.encode(anyString())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void testRegisterUser() {
        when(userDao.existsByEmail(anyString())).thenReturn(false);
        when(userDao.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RegistrationDto registrationDto = new RegistrationDto(
                "test", "test",
                "password", "password",
                "email"
        );

        registrationService.registerNewUserAccount(registrationDto);

        verify(userDao).existsByEmail(registrationDto.email());
        verify(userDao).save(any(User.class));
        verify(passwordEncoder).encode(registrationDto.password());
        verify(roleDao).findByRoleName(RoleName.ROLE_USER);
    }

    @Test
    void testRegisterUserWithExistEmail() {
        when(userDao.existsByEmail(anyString())).thenReturn(true);

        RegistrationDto registrationDto = new RegistrationDto(
                "test", "test",
                "password", "password",
                "existsEmail"
        );

        AlreadyExistsException e = assertThrows(AlreadyExistsException.class,
                () -> registrationService.registerNewUserAccount(registrationDto));

        assertEquals("email", e.getField());
        assertEquals(GenericErrorType.FIELD_VALIDATION_ERROR, e.getErrorType());
        assertNotNull(e.getMessageCode());

        verify(userDao).existsByEmail(registrationDto.email());
    }
}
