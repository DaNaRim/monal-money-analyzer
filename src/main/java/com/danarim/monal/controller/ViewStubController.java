package com.danarim.monal.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Stub controller for testing purposes.
 */
@Controller
@Profile("test")
public class ViewStubController {

    @GetMapping("/internalErrorStub")
    public String internalErrorStub() {
        throw new RuntimeException("internal error stub"); //Unexpected error
    }

}
