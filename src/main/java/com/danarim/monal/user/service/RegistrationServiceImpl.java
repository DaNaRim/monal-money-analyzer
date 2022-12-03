package com.danarim.monal.user.service;

import com.danarim.monal.exceptions.AlreadyExistsException;
import com.danarim.monal.exceptions.BadRequestException;
import com.danarim.monal.user.persistence.dao.RoleDao;
import com.danarim.monal.user.persistence.dao.UserDao;
import com.danarim.monal.user.persistence.model.RoleName;
import com.danarim.monal.user.persistence.model.Token;
import com.danarim.monal.user.persistence.model.User;
import com.danarim.monal.user.web.dto.RegistrationDto;
import com.danarim.monal.util.MailUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;

import static com.danarim.monal.exceptions.GenericErrorType.FIELD_VALIDATION_ERROR;

@Service
@Transactional
public class RegistrationServiceImpl implements RegistrationService {

    private final UserDao userDao;
    private final RoleDao roleDao;
    private final TokenService tokenService;

    private final PasswordEncoder encoder;
    private final MailUtil mailUtil;

    public RegistrationServiceImpl(UserDao userDao,
                                   RoleDao roleDao,
                                   TokenService tokenService,
                                   PasswordEncoder encoder,
                                   MailUtil mailUtil
    ) {
        this.userDao = userDao;
        this.roleDao = roleDao;
        this.tokenService = tokenService;
        this.encoder = encoder;
        this.mailUtil = mailUtil;
    }

    /**
     * Register a new user with role {@link RoleName#ROLE_USER} and not activated account.
     * For activation, user should follow the link in the email.
     *
     * @param registrationDto user data
     * @throws AlreadyExistsException if user with the same email already exists
     */
    @Override
    public User registerNewUserAccount(RegistrationDto registrationDto) {
        if (userDao.existsByEmail(registrationDto.email())) {
            throw new AlreadyExistsException("User with email '" + registrationDto.email() + "' already exists",
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
        return userDao.save(user);
    }

    /**
     * Validate token, activate user account and delete token from database.
     *
     * @param tokenValue token value
     */
    @Override
    public void confirmRegistration(String tokenValue) {
        Token token = tokenService.validateVerificationToken(tokenValue);
        User user = token.getUser();
        user.setEmailVerified(true);
        tokenService.deleteToken(token);
    }

    /**
     * Send email with verification link to user.
     *
     * @param userEmail user email to send verification link
     * @throws BadRequestException if user not found or user already verified
     */
    @Override
    public void resendVerificationToken(String userEmail) {
        User user = userDao.findByEmail(userEmail);

        if (user == null) {
            throw new BadRequestException("Can't find user with email " + userEmail,
                    FIELD_VALIDATION_ERROR,
                    "email",
                    "validation.user.email.notFound",
                    null
            );
        }
        if (user.isEnabled()) {
            throw new BadRequestException("User with email '" + userEmail + "' already verified",
                    FIELD_VALIDATION_ERROR,
                    "email",
                    "validation.user.email.alreadyVerified",
                    null
            );
        }
        Token verificationToken = tokenService.createVerificationToken(user);

        mailUtil.sendVerificationTokenEmail(verificationToken.getTokenValue(), userEmail);
    }
}
