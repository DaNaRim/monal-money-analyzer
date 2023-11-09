package com.danarim.monal.controller;

import com.danarim.monal.config.WebConfig;
import com.danarim.monal.exceptions.ActionDeniedException;
import com.danarim.monal.exceptions.BadFieldException;
import com.danarim.monal.exceptions.BadRequestException;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.MailSendException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Stub controller for testing purposes.
 */
@RestController
@RequestMapping(WebConfig.API_V1_PREFIX)
@Profile("test")
public class StubController {

    @GetMapping("/stub")
    @Secured("ROLE_USER")
    public String stub() {
        return "stub";
    }

    @GetMapping("/adminStub")
    @Secured("ROLE_ADMIN")
    public String authStub() {
        return "authStub";
    }


    @GetMapping("/badRequestStub")
    public String badRequestStub() {
        throw new BadRequestException("Bad request stub", "error.server.internal_error", null);
    }

    @GetMapping("/badFieldStub")
    public String badFieldStub() {
        throw new BadFieldException("Bad request stub", "error.server.internal_error", null,
                                    "field");
    }

    @GetMapping("/accessDeniedStub")
    public String accessDeniedStub() {
        throw new AccessDeniedException("access denied stub");
    }

    @PostMapping("/actionDeniedStub")
    public String actionDeniedStub() {
        throw new ActionDeniedException("action denied stub");
    }

    @GetMapping("/badMailStub")
    public String badMailStub() {
        throw new MailSendException("failed to send mail");
    }

    @GetMapping("/internalErrorStub")
    public String internalErrorStub() {
        throw new RuntimeException("internal error stub"); // Unexpected error
    }

}
