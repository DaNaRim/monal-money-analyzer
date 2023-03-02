package com.danarim.monal.user.service.event;

import com.danarim.monal.user.persistence.model.Token;
import com.danarim.monal.user.persistence.model.TokenType;
import com.danarim.monal.user.persistence.model.User;
import com.danarim.monal.user.service.TokenService;
import com.danarim.monal.util.MailUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RegistrationListenerTest {

    private final TokenService tokenService = mock(TokenService.class);
    private final MailUtil mailUtil = mock(MailUtil.class);

    @InjectMocks
    private RegistrationListener registrationListener;

    @Test
    void onApplicationEvent() {
        User user = mock(User.class);
        Token verificationToken = new Token(user, TokenType.VERIFICATION);

        when(user.getEmail()).thenReturn("userEmail");
        when(tokenService.createVerificationToken(user)).thenReturn(verificationToken);

        registrationListener.onApplicationEvent(new OnRegistrationCompleteEvent(user));

        verify(tokenService).createVerificationToken(user);
        verify(mailUtil).sendVerificationEmail(verificationToken.getTokenValue(), "userEmail");
    }

}
