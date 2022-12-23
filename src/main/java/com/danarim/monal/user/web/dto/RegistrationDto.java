package com.danarim.monal.user.web.dto;

import com.danarim.monal.user.web.validator.PasswordMatches;
import com.danarim.monal.user.web.validator.ValidPassword;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/**
 * Data for user registration.
 *
 * @param firstName        user first name
 * @param lastName         user last name
 * @param password         user password
 * @param matchingPassword confirmation of user password
 * @param email            user email
 */
@PasswordMatches(message = "{validation.user.matching.password}")
public record RegistrationDto(

        @NotBlank(message = "{validation.user.required.firstName}")
        @Size(min = 2, max = 35, message = "{validation.user.size.firstName}")
        String firstName,

        @NotBlank(message = "{validation.user.required.lastName}")
        @Size(min = 2, max = 35, message = "{validation.user.size.lastName}")
        String lastName,

        @NotBlank(message = "{validation.user.required.password}")
        @ValidPassword
        String password,

        @NotBlank(message = "{validation.user.required.confirmPassword}")
        String matchingPassword,

        @NotBlank(message = "{validation.user.required.email}")
        @Pattern(regexp = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$",
                message = "{validation.user.valid.email}")
        @Size(max = 255, message = "{validation.user.size.email}")
        String email

) implements PasswordEntity {

}
