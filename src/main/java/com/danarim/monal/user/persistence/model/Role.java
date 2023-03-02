package com.danarim.monal.user.persistence.model;

import org.springframework.security.core.GrantedAuthority;

import java.io.Serial;
import java.util.Objects;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Represents a role that a user can have.
 */
@Entity
public class Role implements GrantedAuthority {

    @Serial
    private static final long serialVersionUID = -5401171287733128122L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            unique = true,
            nullable = false,
            updatable = false,
            columnDefinition = "VARCHAR(20) CHECK (role_name IN ('ROLE_USER', 'ROLE_ADMIN'))"
    )
    @Enumerated(EnumType.STRING)
    private RoleName roleName;

    protected Role() {
    }

    public Role(RoleName roleName) {
        this.roleName = roleName;
    }

    @Override
    public String getAuthority() {
        return roleName.toString();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RoleName getName() {
        return roleName;
    }

    public void setName(RoleName name) {
        this.roleName = name;
    }

    @Override
    public int hashCode() {
        return Objects.hash(roleName);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Role role = (Role) o;
        return roleName == role.roleName;
    }

}
