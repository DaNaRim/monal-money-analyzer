package com.danarim.monal.util.appmessage;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AppMessageUtilTest {

    @Test
    void resolveAppMessageCode() {
        String code = AppMessageCode.TOKEN_NOT_FOUND.getCode();

        AppMessageCode result = AppMessageUtil.resolveAppMessageCode(code);

        assertEquals(AppMessageCode.TOKEN_NOT_FOUND, result);
    }

    @Test
    void resolveAppMessageCode_unresolvedCode() {
        String code = "unresolvedCode";

        AppMessageCode result = AppMessageUtil.resolveAppMessageCode(code);

        assertEquals(AppMessageCode.UNRESOLVED_CODE, result);
    }

}
