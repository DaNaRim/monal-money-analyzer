package com.danarim.monal.user.service.mail;

import org.springframework.mail.MailException;

/**
 * Responsible for sending account confirmation and password reset emails.
 */
public interface RegistrationMailService {

    /**
     * Create and send email for account verification.
     *
     * @param tokenValue token value
     * @param userEmail  user email to send the email to
     *
     * @throws MailException if there is an error during sending the email
     */
    void sendVerificationEmail(String tokenValue, String userEmail);

    /**
     * Create and send email for password reset.
     *
     * @param tokenValue token value
     * @param userEmail  user email to send the email to
     *
     * @throws MailException if there is an error during sending the email
     */
    void sendPasswordResetEmail(String tokenValue, String userEmail);

}
