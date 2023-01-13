package com.danarim.monal.user.service.event;

import com.danarim.monal.config.security.jwt.JwtUtil;
import com.danarim.monal.user.persistence.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PasswordUpdatedListenerTest {

    private final JwtUtil jwtUtil = mock(JwtUtil.class);

    @InjectMocks
    private PasswordUpdatedListener passwordUpdatedListener;

    @Test
    void onApplicationEvent() {
        User user = mock(User.class);

        passwordUpdatedListener.onApplicationEvent(new OnPasswordUpdatedEvent(user));

        verify(jwtUtil).blockAllTokensForUser(user);
    }
}
