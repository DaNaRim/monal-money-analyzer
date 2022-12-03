package com.danarim.monal.user.service;

import com.danarim.monal.user.persistence.model.User;
import com.danarim.monal.user.web.dto.RegistrationDto;

public interface RegistrationService {

    User registerNewUserAccount(RegistrationDto registrationDto);

    void confirmRegistration(String token);

    void resendVerificationToken(String userEmail);
}
