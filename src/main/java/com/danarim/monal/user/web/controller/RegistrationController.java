package com.danarim.monal.user.web.controller;

import com.danarim.monal.user.persistence.model.User;
import com.danarim.monal.user.service.RegistrationService;
import com.danarim.monal.user.service.event.OnRegistrationCompleteEvent;
import com.danarim.monal.user.web.dto.RegistrationDto;
import com.danarim.monal.util.ApplicationMessage;
import com.danarim.monal.util.ApplicationMessageType;
import com.danarim.monal.util.CookieUtil;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Locale;

import static com.danarim.monal.config.WebConfig.API_V1_PREFIX;

/**
 * Responsible for user registration, activation and password reset.
 */
@RestController
@RequestMapping(API_V1_PREFIX)
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

    @PostMapping("/registration")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerUserAccount(@RequestBody @Valid RegistrationDto registrationDto) {

        User user = registrationService.registerNewUserAccount(registrationDto);
        eventPublisher.publishEvent(new OnRegistrationCompleteEvent(user));
    }

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
                null
        );
        response.addCookie(CookieUtil.createAppMessageCookie(applicationMessage));

        return new RedirectView("/login");
    }

    @PostMapping("/resendVerificationToken")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void resendVerificationToken(@RequestParam("email") String userEmail) {
        registrationService.resendVerificationToken(userEmail);
    }

    @GetMapping("/stub")
    @Secured("ROLE_USER")
    public String stub() {
        return "stub"; //TODO remove
    }

    @GetMapping("/adminStub")
    @Secured("ROLE_ADMIN")
    public String authStub() {
        return "authStub"; //TODO remove
    }

}
