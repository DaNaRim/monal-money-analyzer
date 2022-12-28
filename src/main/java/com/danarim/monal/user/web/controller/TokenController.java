package com.danarim.monal.user.web.controller;

import com.danarim.monal.config.WebConfig;
import com.danarim.monal.user.persistence.model.Token;
import com.danarim.monal.user.service.RegistrationService;
import com.danarim.monal.user.service.TokenService;
import com.danarim.monal.user.web.dto.ResetPasswordDto;
import com.danarim.monal.util.ApplicationMessage;
import com.danarim.monal.util.ApplicationMessageType;
import com.danarim.monal.util.CookieUtil;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * Controller for tokens that was sent to user's email.
 */
@Controller
@RequestMapping(WebConfig.API_V1_PREFIX)
public class TokenController {

    private final RegistrationService registrationService;
    private final TokenService tokenService;
    private final MessageSource messageSource;

    public TokenController(RegistrationService registrationService,
                           TokenService tokenService,
                           MessageSource messageSource
    ) {
        this.registrationService = registrationService;
        this.tokenService = tokenService;
        this.messageSource = messageSource;
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
                messageSource.getMessage("message.registration.confirmation.success", null, locale),
                ApplicationMessageType.INFO,
                "login",
                "message.registration.confirmation.success"
        );
        response.addCookie(CookieUtil.createAppMessageCookie(applicationMessage));

        return new RedirectView("/login");
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

        Token passwordResetToken = tokenService.validatePasswordResetToken(tokenValue);

        response.addCookie(CookieUtil.createPasswordResetCookie(passwordResetToken.getTokenValue()));

        return new RedirectView("/resetPasswordSet");
    }

}
