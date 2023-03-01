package com.danarim.monal.controller;

import com.danarim.monal.config.WebConfig;
import org.springframework.context.annotation.Profile;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
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
}
