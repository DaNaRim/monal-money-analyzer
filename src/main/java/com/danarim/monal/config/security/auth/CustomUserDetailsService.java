package com.danarim.monal.config.security.auth;

import com.danarim.monal.user.persistence.dao.UserDao;
import com.danarim.monal.user.persistence.model.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class CustomUserDetailsService implements UserDetailsService {

    private final UserDao userDao;

    public CustomUserDetailsService(UserDao userDao) {
        this.userDao = userDao;
    }

    /**
     * @param email - username of user
     * @return UserDetails
     * @throws UsernameNotFoundException - if user not found
     */
    @Override
    public UserDetails loadUserByUsername(String email) {
        User user = userDao.findByEmail(email);

        if (user == null) {
            throw new UsernameNotFoundException("No user found with username: " + email);
        }
        return user;
    }

}
