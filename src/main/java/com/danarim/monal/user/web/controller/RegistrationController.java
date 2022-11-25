package com.danarim.monal.user.web.controller;

import com.danarim.monal.user.service.RegistrationService;
import com.danarim.monal.user.web.dto.RegistrationDto;
import com.danarim.monal.util.CookieUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import static com.danarim.monal.config.WebConfig.BACKEND_PREFIX;

/**
 * Responsible for user registration, activation and password reset.
 */
@RestController
@RequestMapping(BACKEND_PREFIX)
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @PostMapping("/registration")
    @ResponseStatus(HttpStatus.CREATED)
    public void registerUserAccount(@RequestBody @Valid RegistrationDto registrationDto) {

        registrationService.registerNewUserAccount(registrationDto);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(HttpServletResponse response) {
        response.addCookie(CookieUtil.deleteAccessTokenCookie());
        response.addCookie(CookieUtil.deleteRefreshTokenCookie());
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
