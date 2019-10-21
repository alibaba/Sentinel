package com.alibaba.jvm.sandbox.qatest.core.util;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

import static com.alibaba.jvm.sandbox.core.util.SandboxStringUtils.toInternalClassName;

public class QaClassUtils {

    /**
     * 目标Class文件转换为字节码数组
     *
     * @param targetClass 目标Class文件
     * @return 目标Class文件字节码数组
     * @throws IOException 转换出错
     */
    public static byte[] toByteArray(final Class<?> targetClass) throws IOException {
        final InputStream is = targetClass.getClassLoader().getResourceAsStream(toResourceName(targetClass.getName()));
        try {
            return IOUtils.toByteArray(is);
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    public static String toResourceName(String javaClassName) {
        return toInternalClassName(javaClassName).concat(".class");
    }

}
