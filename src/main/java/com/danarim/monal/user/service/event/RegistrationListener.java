package com.danarim.monal.user.service.event;

import com.danarim.monal.user.persistence.model.Token;
import com.danarim.monal.user.persistence.model.User;
import com.danarim.monal.user.service.TokenService;
import com.danarim.monal.user.service.mail.RegistrationMailService;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

/**
 * Listener for registration events.
 */
@Component
public class RegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent> {

    private final TokenService tokenService;
    private final RegistrationMailService regMailService;

    public RegistrationListener(TokenService tokenService, RegistrationMailService regMailService) {
        this.tokenService = tokenService;
        this.regMailService = regMailService;
    }

    @Override
    public void onApplicationEvent(OnRegistrationCompleteEvent event) {
        this.sendVerificationTokenToUser(event);
    }

    /**
     * Create a verification token and send it to the user email.
     *
     * @param event the event to be processed
     */
    private void sendVerificationTokenToUser(OnRegistrationCompleteEvent event) {
        User user = event.getUser();
        Token verificationToken = tokenService.createVerificationToken(user);
        regMailService.sendVerificationEmail(verificationToken.getTokenValue(), user.getEmail());
    }

}
