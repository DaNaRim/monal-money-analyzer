package com.danarim.monal.user.persistence.dao;

import com.danarim.monal.user.persistence.model.Role;
import com.danarim.monal.user.persistence.model.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Manages the {@link Role} in the database.
 */
public interface RoleDao extends JpaRepository<Role, Long> {

    Role findByRoleName(RoleName roleName);

}
