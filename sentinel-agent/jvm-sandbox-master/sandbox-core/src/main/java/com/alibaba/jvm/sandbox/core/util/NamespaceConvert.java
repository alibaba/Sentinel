package com.alibaba.jvm.sandbox.core.util;

import ch.qos.logback.classic.PatternLayout;
import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

/**
 * Sandbox的命名空间注册到logback
 */
public class NamespaceConvert extends ClassicConverter {

    private static volatile String namespace;

    @Override
    public String convert(ILoggingEvent event) {
        return null == namespace
                ? "NULL"
                : namespace;
    }

    /**
     * 注册命名空间到Logback
     *
     * @param namespace 命名空间
     */
    public static void initNamespaceConvert(final String namespace) {
        NamespaceConvert.namespace = namespace;
        PatternLayout.defaultConverterMap.put("SANDBOX_NAMESPACE", NamespaceConvert.class.getName());
    }

}
