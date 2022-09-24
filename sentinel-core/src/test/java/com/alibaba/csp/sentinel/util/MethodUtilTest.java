package com.alibaba.csp.sentinel.util;

import java.lang.reflect.Method;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test cases for {@link MethodUtil}.
 *
 * @author Eric Zhao
 */
public class MethodUtilTest {

    @Before
    public void setUp() {
        MethodUtil.clearMethodMap();
    }

    @After
    public void cleanUp() {
        MethodUtil.clearMethodMap();
    }

    @Test
    public void testResolveMethodName() {
        Method fooMethod = null;
        for (Method m : GoodClass.class.getMethods()) {
            if (m.getName().contains("foo")) {
                fooMethod = m;
                break;
            }
        }
        assertNotNull(fooMethod);
        assertEquals("com.alibaba.csp.sentinel.util.MethodUtilTest$GoodClass:foo(long[],java.lang.String,java.lang.Integer[])",
            MethodUtil.resolveMethodName(fooMethod));

        Method bazMethod = null;
        for (Method m : GoodClass.class.getMethods()) {
            if (m.getName().contains("baz")) {
                bazMethod = m;
                break;
            }
        }
        assertNotNull(bazMethod);
        assertEquals("com.alibaba.csp.sentinel.util.MethodUtilTest$GoodClass:baz(double)",
            MethodUtil.resolveMethodName(bazMethod));
    }

    interface GoodClass {
        void foo(long[] p1, String p2, Integer[] p3);

        String baz(double a);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testResolveNullMethod() {
        MethodUtil.resolveMethodName(null);
    }
}