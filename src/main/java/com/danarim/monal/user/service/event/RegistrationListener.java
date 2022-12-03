package com.danarim.monal.user.service.event;

import com.danarim.monal.user.persistence.model.Token;
import com.danarim.monal.user.persistence.model.User;
import com.danarim.monal.user.service.TokenService;
import com.danarim.monal.util.MailUtil;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class RegistrationListener implements ApplicationListener<OnRegistrationCompleteEvent> {

    private final TokenService tokenService;
    private final MailUtil mailUtil;

    public RegistrationListener(TokenService tokenService, MailUtil mailUtil) {
        this.tokenService = tokenService;
        this.mailUtil = mailUtil;
    }

    @Override
    public void onApplicationEvent(OnRegistrationCompleteEvent event) {
        this.sendVerificationTokenToUser(event);
    }

    /**
     * Create a verification token and send it to the user email
     *
     * @param event the event to be processed
     */
    private void sendVerificationTokenToUser(OnRegistrationCompleteEvent event) {
        User user = event.getUser();
        Token verificationToken = tokenService.createVerificationToken(user);
        mailUtil.sendVerificationTokenEmail(verificationToken.getTokenValue(), user.getEmail());
    }
}
