package com.danarim.monal.failhandler;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * Handles 404 errors. Used when request path does not match view controller.
 */
@ControllerAdvice
public class NotFoundExceptionHandler {

    private static final Log logger = LogFactory.getLog(NotFoundExceptionHandler.class);

    /**
     * Handles 404 errors.
     *
     * @param e exception
     *
     * @return Forward to index page. Error will be handled by Frontend router.
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ModelAndView handleNoHandlerFoundException(NoHandlerFoundException e) {
        logger.warn("No handler found for %s %s"
                            .formatted(e.getHttpMethod(), e.getRequestURL()));

        return new ModelAndView("forward:/index.html");
    }

}
