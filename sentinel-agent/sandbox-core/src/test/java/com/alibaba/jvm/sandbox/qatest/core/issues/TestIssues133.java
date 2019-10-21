package com.alibaba.jvm.sandbox.qatest.core.issues;

import org.junit.Assert;
import org.junit.Test;

import static com.alibaba.jvm.sandbox.core.util.AsmUtils.getCommonSuperClass;

public class TestIssues133 {

    @Test
    public void test() {
        final ClassLoader loader = getClass().getClassLoader();
        Assert.assertEquals("java/io/InputStream", getCommonSuperClass("java/io/FileInputStream", "javax/servlet/ServletInputStream", loader));
        Assert.assertEquals("java/lang/Exception", getCommonSuperClass("java/io/IOException", "javax/servlet/ServletException", loader));
        Assert.assertEquals("javax/servlet/ServletResponse", getCommonSuperClass("javax/servlet/ServletResponse", "javax/servlet/http/HttpServletResponse", loader));
        Assert.assertEquals("javax/servlet/ServletResponse", getCommonSuperClass("javax/servlet/http/HttpServletResponse", "javax/servlet/ServletResponse", loader));
        Assert.assertEquals("java/lang/Object", getCommonSuperClass("java/lang/Throwable", "java/io/FileInputStream", loader));
        Assert.assertEquals("java/lang/Exception", getCommonSuperClass("java/io/IOException", "java/lang/Exception", null));
        Assert.assertEquals("java/lang/Exception", getCommonSuperClass("java/io/IOException", "javax/servlet/ServletException", null));
        Assert.assertEquals("java/lang/Object", getCommonSuperClass("java/lang/Throwable", "java/io/FileInputStream", null));
        Assert.assertEquals("javax/servlet/ServletResponse", getCommonSuperClass("javax/servlet/ServletResponse", "javax/servlet/http/HttpServletResponse", null));
        Assert.assertEquals("javax/servlet/ServletResponse", getCommonSuperClass("javax/servlet/http/HttpServletResponse", "javax/servlet/ServletResponse", null));
    }

}
