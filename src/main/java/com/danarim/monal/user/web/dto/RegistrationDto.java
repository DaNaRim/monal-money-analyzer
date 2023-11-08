package com.danarim.monal.user.web.dto;

import com.danarim.monal.exceptions.ValidationCodes;
import com.danarim.monal.user.web.validator.PasswordMatches;
import com.danarim.monal.user.web.validator.ValidPassword;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * Data for user registration.
 *
 * @param password         user password
 * @param matchingPassword confirmation of user password
 * @param email            user email
 */
@PasswordMatches(message = "{validation.user.matching.password}")
public record RegistrationDto(

        @NotBlank(message = ValidationCodes.USER_EMAIL_REQUIRED)
        @Pattern(
                regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$",
                message = ValidationCodes.USER_EMAIL_INVALID
        )
        @Size(max = 255, message = ValidationCodes.USER_EMAIL_SIZE)
        String email,

        @NotBlank(message = ValidationCodes.USER_PASSWORD_REQUIRED)
        @ValidPassword
        String password,

        @NotBlank(message = ValidationCodes.USER_CONFIRM_PASSWORD_REQUIRED)
        String matchingPassword

) implements PasswordEntity {

}
