package com.danarim.monal.user.service;

import com.danarim.monal.exceptions.BadFieldException;
import com.danarim.monal.exceptions.BadRequestException;
import com.danarim.monal.user.persistence.dao.RoleDao;
import com.danarim.monal.user.persistence.dao.UserDao;
import com.danarim.monal.user.persistence.model.RoleName;
import com.danarim.monal.user.persistence.model.Token;
import com.danarim.monal.user.persistence.model.User;
import com.danarim.monal.user.web.dto.RegistrationDto;
import com.danarim.monal.user.web.dto.ResetPasswordDto;
import com.danarim.monal.util.MailUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.Date;

/**
 * Service for registration and basic user operations.
 */
@Service
@Transactional
public class RegistrationServiceImpl implements RegistrationService {

    private final UserDao userDao;
    private final RoleDao roleDao;
    private final TokenService tokenService;

    private final PasswordEncoder passwordEncoder;
    private final MailUtil mailUtil;

    /**
     * Constructor to inject dependencies.
     *
     * @param userDao         user dao to manage users
     * @param roleDao         role dao to get role by name
     * @param tokenService    token service to manage tokens
     * @param passwordEncoder password encoder to encode passwords
     * @param mailUtil        mail util to send emails
     */
    public RegistrationServiceImpl(UserDao userDao,
                                   RoleDao roleDao,
                                   TokenService tokenService,
                                   PasswordEncoder passwordEncoder,
                                   MailUtil mailUtil
    ) {
        this.userDao = userDao;
        this.roleDao = roleDao;
        this.tokenService = tokenService;
        this.passwordEncoder = passwordEncoder;
        this.mailUtil = mailUtil;
    }

    /**
     * Register a new user with role {@link RoleName#ROLE_USER} and not activated account. For
     * activation, user should follow the link in the email.
     *
     * @param registrationDto user data
     *
     * @throws BadFieldException if user with the same email already exists
     */
    @Override
    public User registerNewUserAccount(RegistrationDto registrationDto) {
        if (userDao.existsByEmailIgnoreCase(registrationDto.email())) {
            throw new BadFieldException(
                    "User with email '" + registrationDto.email() + "' already exists",
                    "validation.user.existing.email",
                    null,
                    "email");
        }

        User user = new User(registrationDto.firstName(),
                             registrationDto.lastName(),
                             registrationDto.email(),
                             passwordEncoder.encode(registrationDto.password()),
                             new Date(),
                             //To get role with correct id
                             Collections.singleton(roleDao.findByRoleName(RoleName.ROLE_USER)));
        return userDao.save(user);
    }

    /**
     * Validate token, activate user account and mark token as used.
     *
     * @param tokenValue token value
     */
    @Override
    public void confirmRegistration(String tokenValue) {
        Token token = tokenService.validateVerificationToken(tokenValue);
        User user = token.getUser();
        user.setEmailVerified(true);
        token.setUsed();
    }

    /**
     * Send email with verification link to user.
     *
     * @param userEmail user email to send verification link
     *
     * @throws BadFieldException   if user not found
     * @throws BadRequestException if user already verified
     */
    @Override
    public void resendVerificationEmail(String userEmail) {
        User user = userDao.findByEmailIgnoreCase(userEmail);

        if (user == null) {
            throw new BadFieldException("Can't find user with email '" + userEmail + "'",
                                        "validation.user.email.notFound",
                                        null,
                                        "email");
        }
        if (user.isEnabled()) {
            throw new BadRequestException("User with email '" + userEmail + "' already verified",
                                          "validation.user.email.alreadyVerified",
                                          null);
        }
        Token verificationToken = tokenService.createVerificationToken(user);
        mailUtil.sendVerificationEmail(verificationToken.getTokenValue(), userEmail);
    }

    /**
     * Check if user with this email exists and send email with reset password link.
     *
     * @param userEmail user email to send verification link
     *
     * @throws BadFieldException if user not found
     */
    @Override
    public void resetPassword(String userEmail) {
        User user = userDao.findByEmailIgnoreCase(userEmail);

        if (user == null) {
            throw new BadFieldException("Can't find user with email '" + userEmail + "'",
                                        "validation.user.email.notFound",
                                        null,
                                        "email");
        }
        Token passwordResetToken = tokenService.createPasswordResetToken(user);
        mailUtil.sendPasswordResetEmail(passwordResetToken.getTokenValue(), userEmail);
    }

    /**
     * Validate token, change user password and mark token as used.
     *
     * @param resetPasswordDto   reset password data
     * @param resetPasswordToken password reset token value
     *
     * @return User object if password reset was successful
     *
     * @throws BadFieldException if new password same as old password
     */
    @Override
    public User updateForgottenPassword(ResetPasswordDto resetPasswordDto, String resetPasswordToken
    ) {

        Token token = tokenService.validatePasswordResetToken(resetPasswordToken);
        User user = token.getUser();

        //check if password is not the same as old
        if (passwordEncoder.matches(resetPasswordDto.password(), user.getPassword())) {
            throw new BadFieldException("New password can't be the same as old",
                                        "validation.user.password.sameAsOld",
                                        null,
                                        "password");
        }
        user.setPassword(passwordEncoder.encode(resetPasswordDto.password()));
        token.setUsed();

        return user;
    }

}
