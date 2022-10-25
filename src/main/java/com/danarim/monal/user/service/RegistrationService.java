package com.danarim.monal.user.service;

import com.danarim.monal.user.web.dto.RegistrationDto;

public interface RegistrationService {

    void registerNewUserAccount(RegistrationDto registrationDto);
}
