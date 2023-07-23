package com.danarim.monal.user.persistence.dao;

import com.danarim.monal.user.persistence.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Manages the {@link User} in the database.
 */
public interface UserDao extends JpaRepository<User, Long> {

    User findByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsById(long id);

}
