package com.danarim.monal.failhandler;

import com.danarim.monal.exceptions.InvalidTokenException;
import com.danarim.monal.util.CookieUtil;
import com.danarim.monal.util.appmessage.AppMessage;
import com.danarim.monal.util.appmessage.AppMessageType;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpServletResponse;

import static com.danarim.monal.failhandler.RestExceptionHandler.LOG_TEMPLATE;

/**
 * Handler exceptions thrown by controllers (not rest controllers).
 */
@ControllerAdvice(annotations = Controller.class)
@Order(2)
public class ViewExceptionHandler {

    private static final Log logger = LogFactory.getLog(ViewExceptionHandler.class);

    /**
     * Handles {@link InvalidTokenException} thrown by endpoints that process account activation and
     * password reset.
     *
     * @param e        exception caused by invalid token.
     * @param request  request where exception occurred.
     * @param response http response.
     *
     * @return redirect to login page with error message in cookie
     */
    @ExceptionHandler(InvalidTokenException.class)
    protected View handleInvalidTokenException(InvalidTokenException e,
                                               WebRequest request,
                                               HttpServletResponse response
    ) {
        logger.debug(LOG_TEMPLATE.formatted(e.getClass(), request.getContextPath(), e.getMessage()),
                     e);

        AppMessage applicationMessage =
                new AppMessage(AppMessageType.ERROR,
                               "login",
                               e.getAppMessageCode());
        response.addCookie(CookieUtil.createAppMessageCookie(applicationMessage));

        return new RedirectView("/login");
    }

    /**
     * Handles server exceptions.
     *
     * @param e       exception caused by server error.
     * @param request request where exception occurred.
     *
     * @return redirect to error page
     */
    @ExceptionHandler(Exception.class)
    protected View handleException(Exception e, WebRequest request) {
        logger.error(LOG_TEMPLATE.formatted(e.getClass(), request.getContextPath(), e.getMessage()),
                     e);
        return new RedirectView("/error");
    }

}
