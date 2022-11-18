package com.danarim.monal.user.web.controller;

import com.danarim.monal.user.service.RegistrationService;
import com.danarim.monal.user.web.dto.RegistrationDto;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

import static com.danarim.monal.config.WebConfig.BACKEND_PREFIX;

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
