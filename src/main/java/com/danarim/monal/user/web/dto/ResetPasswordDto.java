package com.danarim.monal.user.web.dto;

import com.danarim.monal.user.web.validator.PasswordMatches;
import com.danarim.monal.user.web.validator.ValidPassword;

import javax.validation.constraints.NotBlank;

/**
 * Data to update user password that was forgotten.
 *
 * @param newPassword      new password to be set
 * @param matchingPassword confirmation of the new password
 */
@PasswordMatches(message = "{validation.user.matching.password}")
public record ResetPasswordDto(

        @NotBlank(message = "{validation.user.required.newPassword}")
        @ValidPassword
        String newPassword,

        @NotBlank(message = "{validation.user.required.matchingPassword}")
        String matchingPassword

) implements PasswordEntity {

    @Override
    public String password() {
        return newPassword;
    }

}
