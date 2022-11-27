package com.danarim.monal.config.filters;

import com.danarim.monal.exceptions.GenericErrorType;
import com.danarim.monal.failHandler.GenericErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.LocaleResolver;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;

/**
 * Handle all exceptions that are not handled by other filters or handlers
 */
@Component
public class ExceptionHandlerFilter extends OncePerRequestFilter {

    private final MessageSource messages;
    private final LocaleResolver localeResolver;

    public ExceptionHandlerFilter(MessageSource messages, LocaleResolver localeResolver) {
        this.messages = messages;
        this.localeResolver = localeResolver;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (RuntimeException e) {
            logger.error("Unexpected error caught by ExceptionHandlerFilter: " + e.getMessage(), e);

            Locale locale = localeResolver.resolveLocale(request);

            GenericErrorResponse errorResponse = new GenericErrorResponse(
                    GenericErrorType.SERVER_ERROR.getType(),
                    GenericErrorType.SERVER_ERROR.getType(),
                    messages.getMessage("error.server.internal-error", null, locale)
            );
            response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
            new ObjectMapper().writeValue(response.getOutputStream(), errorResponse);
        }
    }
}
