package com.danarim.monal.util;

import com.danarim.monal.config.WebConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.Locale;

@Component
public class MailUtil {

    private static final String ACCOUNT_CONFIRM_ENDPOINT = "/registrationConfirm";
    private static final String PASSWORD_RESET_ENDPOINT = "/resetPasswordConfirm";

    private static final String TOKEN_LINK_TEMPLATE = "%s%s%s?token=%s";

    private final MessageSource messages;
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String from;

    public MailUtil(MessageSource messages, JavaMailSender mailSender) {
        this.messages = messages;
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String tokenValue, String userEmail) {

        Locale locale = LocaleContextHolder.getLocale();
        String contextPath = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();

        String confirmUrl = String.format(TOKEN_LINK_TEMPLATE,
                contextPath,
                WebConfig.API_V1_PREFIX,
                ACCOUNT_CONFIRM_ENDPOINT,
                tokenValue);

        String subject = messages.getMessage("mail.verifyAccount.subject", null, locale);
        String message = messages.getMessage("mail.verifyAccount.link.enable", null, locale);

        mailSender.send(constructEmail(subject, message + "\r\n" + confirmUrl, userEmail));
    }

    public void sendPasswordResetEmail(String tokenValue, String userEmail) {

        Locale locale = LocaleContextHolder.getLocale();
        String contextPath = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();

        String confirmUrl = String.format(TOKEN_LINK_TEMPLATE,
                contextPath,
                WebConfig.API_V1_PREFIX,
                PASSWORD_RESET_ENDPOINT,
                tokenValue);

        String subject = messages.getMessage("mail.resetPassword.subject", null, locale);
        String message = messages.getMessage("mail.resetPassword.link.reset", null, locale);

        mailSender.send(constructEmail(subject, message + "\r\n" + confirmUrl, userEmail));
    }

    private SimpleMailMessage constructEmail(String subject, String body, String userEmail) {
        SimpleMailMessage email = new SimpleMailMessage();
        email.setSubject(subject);
        email.setText(body);
        email.setTo(userEmail);
        email.setFrom(from);
        return email;
    }

}
