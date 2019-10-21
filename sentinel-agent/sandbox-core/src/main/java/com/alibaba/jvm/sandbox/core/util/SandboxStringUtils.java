package com.alibaba.jvm.sandbox.core.util;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.Type;

import java.io.IOException;
import java.io.InputStream;

/**
 * 字符串工具类
 * Created by luanjia@taobao.com on 15/5/18.
 */
public class SandboxStringUtils {

    /**
     * java's classname to internal's classname
     *
     * @param javaClassName java's classname
     * @return internal's classname
     */
    public static String toInternalClassName(String javaClassName) {
        if (StringUtils.isEmpty(javaClassName)) {
            return javaClassName;
        }
        return javaClassName.replace('.', '/');    }

    /**
     * internal's classname to java's classname
     * java/lang/String to java.lang.String
     *
     * @param internalClassName internal's classname
     * @return java's classname
     */
    public static String toJavaClassName(String internalClassName) {
        if (StringUtils.isEmpty(internalClassName)) {
            return internalClassName;
        }
        return internalClassName.replace('/', '.');    }

    public static String[] toJavaClassNameArray(String[] internalClassNameArray) {
        if (null == internalClassNameArray) {
            return new String[]{};
        }
        final String[] javaClassNameArray = new String[internalClassNameArray.length];
        for (int index = 0; index < internalClassNameArray.length; index++) {
            javaClassNameArray[index] = toJavaClassName(internalClassNameArray[index]);
        }
        return javaClassNameArray;
    }

    public static String[] toJavaClassNameArray(Type[] asmTypeArray) {
        if (null == asmTypeArray) {
            return new String[]{};
        }
        final String[] javaClassNameArray = new String[asmTypeArray.length];
        for (int index = 0; index < asmTypeArray.length; index++) {
            javaClassNameArray[index] = asmTypeArray[index].getClassName();
        }
        return javaClassNameArray;
    }

    /**
     * 获取异常的原因描述
     *
     * @param t 异常
     * @return 异常原因
     */
    public static String getCauseMessage(Throwable t) {
        if (null != t.getCause()) {
            return getCauseMessage(t.getCause());
        }
        return t.getMessage();
    }

    /**
     * 获取LOGO
     *
     * @return LOGO
     */
    public static String getLogo() {
        try {
            final InputStream logoIs = SandboxStringUtils.class.getResourceAsStream("/com/alibaba/jvm/sandbox/logo");
            final String logo = IOUtils.toString(logoIs);
            IOUtils.closeQuietly(logoIs);
            return logo;
        } catch (IOException ioe) {
            // ignore...
            return StringUtils.EMPTY;
        }
    }

}

