package com.danarim.monal.user.service.event;

import com.danarim.monal.user.persistence.model.*;
import com.danarim.monal.user.service.TokenService;
import com.danarim.monal.util.MailUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegistrationListenerTest {

    private final TokenService tokenService = mock(TokenService.class);
    private final MailUtil mailUtil = mock(MailUtil.class);

    @InjectMocks
    private RegistrationListener registrationListener;

    @Test
    void onApplicationEvent() {
        User user = new User(
                "test", "test",
                "userEmail", "password",
                new Date(), Set.of(new Role(RoleName.ROLE_USER))
        );
        Token verificationToken = new Token(user, TokenType.VERIFICATION);

        when(tokenService.createVerificationToken(user)).thenReturn(verificationToken);

        registrationListener.onApplicationEvent(new OnRegistrationCompleteEvent(user));

        verify(tokenService).createVerificationToken(user);
        verify(mailUtil).sendVerificationTokenEmail(verificationToken.getTokenValue(), "userEmail");
    }
}
