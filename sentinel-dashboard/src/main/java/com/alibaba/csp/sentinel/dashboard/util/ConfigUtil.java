package com.alibaba.csp.sentinel.dashboard.util;

import org.apache.commons.lang.StringUtils;

public class ConfigUtil {

    public static String getConfig(String name) {
        return getConfig(name, "");
    }

    public static String getConfig(String name, String defaultVal) {
        // env
        String val = System.getenv(name.replaceAll("\\.","_"));
        if (StringUtils.isNotEmpty(val)) {
            return val;
        }
        // properties
        val = System.getProperty(name);
        if (StringUtils.isNotEmpty(val)) {
            return val;
        }
        return defaultVal;
    }
}
