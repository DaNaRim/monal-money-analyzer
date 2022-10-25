package com.danarim.monal.user.persistence.model;

public enum RoleName {
    USER,
    ADMIN;

    @Override
    public String toString() {
        return "ROLE_" + name();
    }
}
