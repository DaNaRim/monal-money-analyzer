package com.danarim.monal.user.service;

import com.danarim.monal.exceptions.AlreadyExistsException;
import com.danarim.monal.user.persistence.dao.RoleDao;
import com.danarim.monal.user.persistence.dao.UserDao;
import com.danarim.monal.user.persistence.model.RoleName;
import com.danarim.monal.user.persistence.model.User;
import com.danarim.monal.user.web.dto.RegistrationDto;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.Date;

import static com.danarim.monal.exceptions.GenericErrorType.FIELD_VALIDATION_ERROR;

@Service
public class RegistrationServiceImpl implements RegistrationService {

    private final UserDao userDao;
    private final RoleDao roleDao;

    private final PasswordEncoder encoder;

    public RegistrationServiceImpl(UserDao userDao, RoleDao roleDao, PasswordEncoder encoder) {
        this.userDao = userDao;
        this.roleDao = roleDao;
        this.encoder = encoder;
    }

    /**
     * Register a new user with role {@link RoleName#ROLE_USER} and not activated account.
     * For activation, user should follow the link in the email.
     *
     * @param registrationDto user data
     * @throws AlreadyExistsException if user with the same email already exists
     */
    @Override
    public void registerNewUserAccount(RegistrationDto registrationDto) {
        if (userDao.existsByEmail(registrationDto.email())) {
            throw new AlreadyExistsException(
                    "User with this email already exists",
                    FIELD_VALIDATION_ERROR,
                    "email",
                    "validation.user.existing.email",
                    null
            );
        }

        User user = new User(
                registrationDto.firstName(),
                registrationDto.lastName(),
                registrationDto.email(),
                encoder.encode(registrationDto.password()),
                new Date(),
                Collections.singleton(roleDao.findByRoleName(RoleName.ROLE_USER)) //To get role with correct id
        );
        userDao.save(user);
    }
}
