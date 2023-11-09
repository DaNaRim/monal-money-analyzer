package com.danarim.monal.user.web.dto;

import com.danarim.monal.exceptions.ValidationCodes;
import com.danarim.monal.user.web.validator.PasswordMatches;
import com.danarim.monal.user.web.validator.ValidPassword;

import javax.validation.constraints.NotBlank;

/**
 * Data to update user password that was forgotten.
 *
 * @param newPassword      new password to be set
 * @param matchingPassword confirmation of the new password
 */
@PasswordMatches(message = ValidationCodes.USER_PASSWORD_MATCHING)
public record ResetPasswordDto(

        @NotBlank(message = ValidationCodes.USER_NEW_PASSWORD_REQUIRED)
        @ValidPassword
        String newPassword,

        @NotBlank(message = ValidationCodes.USER_CONFIRM_PASSWORD_REQUIRED)
        String matchingPassword

) implements PasswordEntity {

    @Override
    public String password() {
        return newPassword;
    }

}
