package com.danarim.monal.user.service.mail;

import com.danarim.monal.config.WebConfig;
import com.danarim.monal.user.web.controller.TokenController;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.mail.MailPreparationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Map;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;


/**
 * Uses simple mail messages to send emails.
 */
@Component
public class RegistrationMailServiceImpl implements RegistrationMailService {

    private static final String TOKEN_LINK_TEMPLATE = "%s%s%s?token=%s";

    private static final String ACCOUNT_CONFIRMATION_TEMPLATE = "accountConfirmationEmail";
    private static final String PASSWORD_RESET_TEMPLATE = "passwordResetEmail";

    private final MessageSource messages;
    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String from;

    /**
     * Dependency injection constructor.
     *
     * @param messages       message source for i18n
     * @param mailSender     mail sender
     * @param templateEngine template engine for html emails
     */
    public RegistrationMailServiceImpl(MessageSource messages, JavaMailSender mailSender,
                                       SpringTemplateEngine templateEngine
    ) {
        this.messages = messages;
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    @Override
    public void sendVerificationEmail(String tokenValue, String userEmail) {

        Locale locale = LocaleContextHolder.getLocale();
        String contextPath =
                ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();

        String confirmUrl = String.format(TOKEN_LINK_TEMPLATE,
                                          contextPath,
                                          WebConfig.API_V1_PREFIX,
                                          TokenController.ACCOUNT_CONFIRM_ENDPOINT,
                                          tokenValue);

        Map<String, Object> model = Map.of(
                "subject", messages.getMessage("mail.verifyAccount.subject", null, locale),
                "header", messages.getMessage("mail.verifyAccount.header", null, locale),
                "body", messages.getMessage("mail.verifyAccount.body", null, locale),
                "confirmationUrl", confirmUrl,
                "confirmationUrlText",
                messages.getMessage("mail.verifyAccount.link.enable", null, locale)
        );

        MimeMessage mimeMessage = constructEmail(userEmail, ACCOUNT_CONFIRMATION_TEMPLATE, model);
        mailSender.send(mimeMessage);
    }

    @Override
    public void sendPasswordResetEmail(String tokenValue, String userEmail) {

        Locale locale = LocaleContextHolder.getLocale();
        String contextPath =
                ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString();

        String confirmUrl = String.format(TOKEN_LINK_TEMPLATE,
                                          contextPath,
                                          WebConfig.API_V1_PREFIX,
                                          TokenController.PASSWORD_RESET_ENDPOINT,
                                          tokenValue);

        Map<String, Object> model = Map.of(
                "subject", messages.getMessage("mail.resetPassword.subject", null, locale),
                "header", messages.getMessage("mail.resetPassword.header", null, locale),
                "body", messages.getMessage("mail.resetPassword.body", null, locale),
                "resetUrl", confirmUrl,
                "resetUrlText",
                messages.getMessage("mail.resetPassword.link.reset", null, locale)
        );

        MimeMessage mimeMessage = constructEmail(userEmail, PASSWORD_RESET_TEMPLATE, model);
        mailSender.send(mimeMessage);
    }

    private MimeMessage constructEmail(String userEmail, String template, Map<String, Object> model
    ) {
        MimeMessage mimeMessage = mailSender.createMimeMessage();
        try {
            MimeMessageHelper helper = new MimeMessageHelper(
                    mimeMessage,
                    MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                    StandardCharsets.UTF_8.name()
            );
            Context context = new Context();
            context.setVariables(model);

            helper.setFrom(from);
            helper.setTo(userEmail);
            helper.setSubject(model.get("subject").toString());
            String html = templateEngine.process(template, context);
            helper.setText(html, true);
        } catch (MessagingException e) {
            throw new MailPreparationException("Failed to create email", e);
        }
        return mimeMessage;
    }

}
