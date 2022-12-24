package com.danarim.monal.user.web.controller;

import com.danarim.monal.config.WebConfig;
import com.danarim.monal.user.persistence.model.Token;
import com.danarim.monal.user.persistence.model.User;
import com.danarim.monal.user.service.RegistrationService;
import com.danarim.monal.user.service.event.OnPasswordUpdatedEvent;
import com.danarim.monal.user.service.event.OnRegistrationCompleteEvent;
import com.danarim.monal.user.web.dto.RegistrationDto;
import com.danarim.monal.user.web.dto.ResetPasswordDto;
import com.danarim.monal.util.ApplicationMessage;
import com.danarim.monal.util.ApplicationMessageType;
import com.danarim.monal.util.CookieUtil;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

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
    private final MessageSource messages;

    public RegistrationController(RegistrationService registrationService,
                                  ApplicationEventPublisher eventPublisher,
                                  MessageSource messages
    ) {
        this.registrationService = registrationService;
        this.eventPublisher = eventPublisher;
        this.messages = messages;
    }

    /**
     * Uses to register a new user account. Sends an email with activation link.
     *
     * @param registrationDto user data
     * @see RegistrationController#confirmRegistration(String, Locale, HttpServletResponse)
     */
    @PostMapping("/registration")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerUserAccount(@RequestBody @Valid RegistrationDto registrationDto) {

        User user = registrationService.registerNewUserAccount(registrationDto);
        eventPublisher.publishEvent(new OnRegistrationCompleteEvent(user));
    }

    /**
     * Uses to activate user account. Activates user account and redirects to the login page.
     * Adds a cookie with a message about successful activation.
     *
     * @param tokenValue token from link in email
     * @param locale     locale from request
     * @param response   response to set cookie
     * @return redirect to login page
     */
    @GetMapping("/registrationConfirm")
    public View confirmRegistration(@RequestParam("token") String tokenValue,
                                    Locale locale,
                                    HttpServletResponse response
    ) {
        registrationService.confirmRegistration(tokenValue);

        ApplicationMessage applicationMessage = new ApplicationMessage(
                messages.getMessage("message.registration.confirmation.success", null, locale),
                ApplicationMessageType.INFO,
                "login",
                "message.registration.confirmation.success"
        );
        response.addCookie(CookieUtil.createAppMessageCookie(applicationMessage));

        return new RedirectView("/login");
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
     * @see RegistrationController#resetPasswordConfirm(String, HttpServletResponse)
     * @see RegistrationController#resetPasswordSet(ResetPasswordDto, HttpServletRequest, HttpServletResponse)
     */
    @PostMapping("/resetPassword")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resetPassword(@RequestParam("email") String userEmail) {
        registrationService.resetPassword(userEmail);
    }

    /**
     * Link from email to reset password. Step 2 during password reset.
     *
     * @param tokenValue token from link
     * @param response   http response
     * @return redirect to reset password page
     * @see RegistrationController#resetPassword(String)
     * @see RegistrationController#resetPasswordSet(ResetPasswordDto, HttpServletRequest, HttpServletResponse)
     */
    @GetMapping("/resetPasswordConfirm")
    public View resetPasswordConfirm(@RequestParam("token") String tokenValue, HttpServletResponse response) {

        Token passwordResetToken = registrationService.validatePasswordResetToken(tokenValue);

        response.addCookie(CookieUtil.createPasswordResetCookie(passwordResetToken.getTokenValue()));

        return new RedirectView("/resetPasswordSet");
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
