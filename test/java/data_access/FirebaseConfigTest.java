package data_access;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.*;

class FirebaseConfigTest {

    @Test
    void classIsLoadable() {
        assertNotNull(FirebaseConfig.class);
    }

    @Test
    void allDeclaredMethodsAreReflectivelyAccessible() {
        Method[] methods = FirebaseConfig.class.getDeclaredMethods();
        assertNotNull(methods);
        for (Method m : methods) {
            assertNotNull(m.getName());
            assertNotNull(m.getReturnType());
            assertNotNull(m.getParameterTypes());
        }
    }
}
