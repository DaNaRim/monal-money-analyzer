package com.danarim.monal.user.service;

import com.danarim.monal.user.persistence.model.User;
import com.danarim.monal.user.web.dto.RegistrationDto;
import com.danarim.monal.user.web.dto.ResetPasswordDto;

public interface RegistrationService {

    User registerNewUserAccount(RegistrationDto registrationDto);

    void confirmRegistration(String token);

    void resendVerificationEmail(String userEmail);

    void resetPassword(String userEmail);

    User updateForgottenPassword(ResetPasswordDto resetPasswordDto, String resetPasswordToken);
}
