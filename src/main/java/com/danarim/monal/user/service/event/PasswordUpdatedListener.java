package com.danarim.monal.user.service.event;

import com.danarim.monal.config.security.jwt.JwtUtil;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Listener for password updated events.
 */
@Component
public class PasswordUpdatedListener implements ApplicationListener<OnPasswordUpdatedEvent> {

    private final JwtUtil jwtUtil;

    public PasswordUpdatedListener(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public void onApplicationEvent(OnPasswordUpdatedEvent event) {
        this.invalidateAllTokens(event);
    }

    /**
     * Invalidate all JWT tokens for the user.
     *
     * @param event the event to be processed
     */
    private void invalidateAllTokens(OnPasswordUpdatedEvent event) {
        jwtUtil.blockAllTokensForUser(event.getUser());
    }

}
