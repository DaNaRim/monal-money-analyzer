package com.danarim.monal.util.appmessage;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AppMessageUtilTest {

    @Test
    void createInstance_AssertionError() throws Exception {
        Constructor<AppMessageUtil> constructor = AppMessageUtil.class.getDeclaredConstructor();
        assertTrue(Modifier.isPrivate(constructor.getModifiers()));
        constructor.setAccessible(true);
        assertThrows(InvocationTargetException.class, constructor::newInstance);
    }

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
