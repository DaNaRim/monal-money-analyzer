package com.danarim.monal.util.appmessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.stream.Stream;

/**
 * Utility class for creating {@link AppMessage}.
 */
public final class AppMessageUtil {

    private static final Log logger = LogFactory.getLog(AppMessageUtil.class);

    private AppMessageUtil() {
        throw new AssertionError("No AppMessageUtil instances for you!");
    }

    /**
     * Checks if message code is valid and if not, reports error and uses
     * {@link AppMessageCode#UNRESOLVED_CODE} instead.
     *
     * @param messageCode used to identify message in frontend.
     *
     * @return {@link AppMessageCode} for given message code or
     *         {@link AppMessageCode#UNRESOLVED_CODE} if message code is not valid.
     */
    public static AppMessageCode resolveAppMessageCode(String messageCode) {
        return Stream.of(AppMessageCode.values())
                .filter(code0 -> code0.getCode().equals(messageCode))
                .findFirst()
                .orElseGet(() -> {
                    logger.error("Can't find message code for: " + messageCode
                                         + ". Code must be defined in ApplicationMessageCode enum. "
                                         + "Falling back to default error code."
                    );
                    return AppMessageCode.UNRESOLVED_CODE;
                });
    }

}
