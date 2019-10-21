package com.alibaba.csp.ahas.sentinel.util;

/**
 * get AHAS_APP_NAME from system property.
 *
 * @author leyou 2019/4/23
 */
public class AhasAppNameUtil {
    public static final String AHAS_APP_NAME = "ahas.project.name";
    private static String ahasAppName;

    static {
        resolveEdasAppId();
    }

    public static void resolveEdasAppId() {
        String app = System.getProperty(AHAS_APP_NAME);
        if (!isEmpty(app)) {
            ahasAppName = app;
        }
    }

    public static String getAhasAppName() {
        return ahasAppName;
    }

    private static boolean isEmpty(String str) {
        return str == null || "".equals(str);
    }
}
