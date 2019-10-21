package com.alibaba.jvm.sandbox.api.util;

import org.junit.Assert;
import org.junit.Test;

public class GaStringUtilsTests {

    @Test
    public void testGetJavaClassName() {
        Assert.assertEquals("java.lang.String",
                GaStringUtils.getJavaClassName(String.class));
    }

    @Test
    public void testGetJavaClassNameArray() {
        Assert.assertNull(GaStringUtils.getJavaClassNameArray(null));
        Assert.assertNull(GaStringUtils.getJavaClassNameArray(new Class[]{}));

        Class[] classes = new Class[]{String.class, Integer.class};
        String[] strings =
                new String[]{"java.lang.String", "java.lang.Integer"};

        Assert.assertArrayEquals(strings,
                GaStringUtils.getJavaClassNameArray(classes));
    }

    @Test
    public void testIsEmpty() {
        Assert.assertTrue(GaStringUtils.isEmpty(""));
        Assert.assertTrue(GaStringUtils.isEmpty(null));

        Assert.assertFalse(GaStringUtils.isEmpty("foo"));
    }

    @Test
    public void testMatching() {
        Assert.assertFalse(GaStringUtils.matching(null, "bar"));
        Assert.assertFalse(GaStringUtils.matching("foo", null));
        Assert.assertFalse(GaStringUtils.matching(null, null));
        Assert.assertFalse(GaStringUtils.matching("foo", "bar"));
        Assert.assertFalse(GaStringUtils.matching("foobar", "foo"));
        Assert.assertFalse(GaStringUtils.matching("foobar", "*a"));
        Assert.assertFalse(GaStringUtils.matching("foo", "\\o"));
        Assert.assertFalse(GaStringUtils.matching("foo", "\\*"));
        Assert.assertFalse(GaStringUtils.matching("foo", "f\\?o"));
        Assert.assertFalse(GaStringUtils.matching("fooMatching", "fool\\*ing"));

        Assert.assertTrue(GaStringUtils.matching("foo", "*"));
        Assert.assertTrue(GaStringUtils.matching("foo", "?oo"));
        Assert.assertTrue(GaStringUtils.matching("foo", "**o"));
        Assert.assertTrue(GaStringUtils.matching("foo", "f?o"));
        Assert.assertTrue(GaStringUtils.matching("fooMatching", "foo*"));
        Assert.assertTrue(GaStringUtils.matching("fooMatching", "foo*ing"));
    }
}
