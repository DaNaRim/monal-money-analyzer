package com.danarim.monal.user.web.controller;

import com.danarim.monal.config.WebConfig;
import com.danarim.monal.user.persistence.model.User;
import com.danarim.monal.user.service.RegistrationService;
import com.danarim.monal.user.service.event.OnPasswordUpdatedEvent;
import com.danarim.monal.user.service.event.OnRegistrationCompleteEvent;
import com.danarim.monal.user.web.dto.RegistrationDto;
import com.danarim.monal.user.web.dto.ResetPasswordDto;
import com.danarim.monal.util.CookieUtil;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Locale;

/**
 * Responsible for user registration, activation and password reset (forgot password).
 */
@RestController
@RequestMapping(WebConfig.API_V1_PREFIX)
public class RegistrationController {

    private final RegistrationService registrationService;
    private final ApplicationEventPublisher eventPublisher;

    public RegistrationController(RegistrationService registrationService, ApplicationEventPublisher eventPublisher) {
        this.registrationService = registrationService;
        this.eventPublisher = eventPublisher;
    }

    /**
     * Uses to register a new user account. Sends an email with activation link.
     *
     * @param registrationDto user data
     * @see TokenController#confirmRegistration(String, Locale, HttpServletResponse)
     */
    @PostMapping("/registration")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerUserAccount(@RequestBody @Valid RegistrationDto registrationDto) {

        User user = registrationService.registerNewUserAccount(registrationDto);
        eventPublisher.publishEvent(new OnRegistrationCompleteEvent(user));
    }

    /**
     * Sends an email with activation link.
     * Uses when previous activation link is expired or other problem with activation.
     *
     * @param userEmail email of user to resend activation link
     */
    @PostMapping("/resendVerificationToken")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resendVerificationToken(@RequestParam("email") String userEmail) {
        registrationService.resendVerificationEmail(userEmail);
    }

    /**
     * Used when user forgot password. Step 1 during password reset.
     *
     * @param userEmail email to send password reset email
     * @see TokenController#resetPasswordConfirm(String, HttpServletResponse)
     * @see RegistrationController#resetPasswordSet(ResetPasswordDto, HttpServletRequest, HttpServletResponse)
     */
    @PostMapping("/resetPassword")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@RequestParam("email") String userEmail) {
        registrationService.resetPassword(userEmail);
    }

    /**
     * Uses from frontend to update forgotten password. Step 3 during password reset.
     *
     * @param resetPasswordDto password reset dto
     * @param request          http request
     * @param response         http response
     */
    @PostMapping("/resetPasswordSet")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPasswordSet(@RequestBody @Valid ResetPasswordDto resetPasswordDto,
                                 HttpServletRequest request,
                                 HttpServletResponse response
    ) {
        String resetPasswordToken = CookieUtil.getPasswordResetTokenValueByRequest(request);

        User user = registrationService.updateForgottenPassword(resetPasswordDto, resetPasswordToken);

        response.addCookie(CookieUtil.deletePasswordResetCookie());

        eventPublisher.publishEvent(new OnPasswordUpdatedEvent(user));
    }
}
