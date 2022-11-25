package com.danarim.monal.config.security.auth;

import com.danarim.monal.user.persistence.model.User;

import java.util.Arrays;
import java.util.Objects;

/**
 * Uses for managing auth on client side
 */
public record AuthResponseEntity(
        String username,
        String firstName,
        String lastName,
        String[] roles
) {
    public static AuthResponseEntity generateAuthResponse(User user) {
        return new AuthResponseEntity(
                user.getUsername(),
                user.getFirstName(),
                user.getLastName(),
                user.getRoles().stream()
                        .map(role -> role.getName().toString())
                        .toArray(String[]::new)
        );
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        AuthResponseEntity that = (AuthResponseEntity) o;
        return Objects.equals(username, that.username) && Objects.equals(firstName,
                that.firstName) && Objects.equals(lastName, that.lastName) && Arrays.equals(roles,
                that.roles);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(username, firstName, lastName);
        result = 31 * result + Arrays.hashCode(roles);
        return result;
    }

    @Override
    public String toString() {
        return "AuthResponseEntity{" +
                "username='" + username + '\'' +
                ", firstName='" + firstName + '\'' +
                ", lastName='" + lastName + '\'' +
                ", roles=" + Arrays.toString(roles) +
                '}';
    }
}
