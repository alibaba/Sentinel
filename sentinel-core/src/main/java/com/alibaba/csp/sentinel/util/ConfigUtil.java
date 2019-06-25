package com.alibaba.csp.sentinel.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * <p>
 * util for loading config
 * </p>
 *
 * @author lianglin
 * @since 1.7.0
 */
public final class ConfigUtil {


    public static Properties loadPropertiesFromFile(String fileName) {
        if (StringUtil.isNotBlank(fileName)) {
            if (fileName.startsWith("/")) {
                return loadPropertiesFromAbsoluteFile(fileName);
            } else {
                return loadPropertiesFromRelativeFile(fileName);
            }
        } else {
            return null;
        }
    }

    private static Properties loadPropertiesFromAbsoluteFile(String fileName) {
        Properties properties = null;
        try {
            FileInputStream input = new FileInputStream(fileName);
            try {
                properties = new Properties();
                properties.load(input);
            } finally {
                input.close();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return properties;
    }


    private static Properties loadPropertiesFromRelativeFile(String fileName) {
        List<URL> list = new ArrayList<URL>();
        try {
            Enumeration<URL> urls = getClassLoader().getResources(fileName);
            list = new ArrayList<URL>();
            while (urls.hasMoreElements()) {
                list.add(urls.nextElement());
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        Properties properties = new Properties();
        for (URL url : list) {
            try {
                Properties p = new Properties();
                InputStream input = url.openStream();
                if (input != null) {
                    try {
                        p.load(input);
                        properties.putAll(p);
                    } finally {
                        try {
                            input.close();
                        } catch (Throwable t) {
                        }
                    }
                }
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return properties;
    }

    private static ClassLoader getClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = ConfigUtil.class.getClassLoader();
        }
        return classLoader;
    }

    public static String addSeparator(String logDir) {
        if (!logDir.endsWith(File.separator)) {
            logDir += File.separator;
        }
        return logDir;
    }


}
