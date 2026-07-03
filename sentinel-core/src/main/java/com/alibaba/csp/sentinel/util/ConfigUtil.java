/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;

/**
 * <p>
 * Util class for loading configuration from file or command arguments.
 * </p>
 *
 * @author lianglin
 * @since 1.7.0
 */
public final class ConfigUtil {

    public static final String CLASSPATH_FILE_FLAG = "classpath:";

    /**
     * <p>Load the properties from provided file.</p>
     * <p>Currently it supports reading from classpath file or local file.</p>
     *
     * @param fileName valid file path
     * @return the retrieved properties from the file; null if the file not exist
     */
    public static Properties loadProperties(String fileName) {
        if (StringUtil.isNotBlank(fileName)) {
            if (absolutePathStart(fileName)) {
                return loadPropertiesFromAbsoluteFile(fileName);
            } else if (fileName.startsWith(CLASSPATH_FILE_FLAG)) {
                return loadPropertiesFromClasspathFile(fileName);
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

            File file = new File(fileName);
            if (!file.exists()) {
                return null;
            }

            try (BufferedReader bufferedReader =
                         new BufferedReader(new InputStreamReader(new FileInputStream(file), getCharset()))) {
                properties = new Properties();
                properties.load(bufferedReader);
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return properties;
    }

    private static boolean absolutePathStart(String path) {
        File[] files = File.listRoots();
        for (File file : files) {
            if (path.startsWith(file.getPath())) {
                return true;
            }
        }
        return false;
    }


    private static Properties loadPropertiesFromClasspathFile(String fileName) {
        fileName = fileName.substring(CLASSPATH_FILE_FLAG.length()).trim();

        List<URL> list = new ArrayList<>();
        try {
            Enumeration<URL> urls = getClassLoader().getResources(fileName);
            list = new ArrayList<>();
            while (urls.hasMoreElements()) {
                list.add(urls.nextElement());
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }

        if (list.isEmpty()) {
            return null;
        }

        Properties properties = new Properties();
        for (URL url : list) {
            try (BufferedReader bufferedReader =
                         new BufferedReader(new InputStreamReader(url.openStream(), getCharset()))) {
                Properties p = new Properties();
                p.load(bufferedReader);
                properties.putAll(p);
            } catch (Throwable e) {
                e.printStackTrace();
            }
        }
        return properties;
    }

    private static Properties loadPropertiesFromRelativeFile(String fileName) {
        String userDir = System.getProperty("user.dir");
        String realFilePath = addSeparator(userDir) + fileName;
        return loadPropertiesFromAbsoluteFile(realFilePath);
    }

    private static ClassLoader getClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = ConfigUtil.class.getClassLoader();
        }
        return classLoader;
    }

    private static Charset getCharset() {
        // avoid static loop dependencies: SentinelConfig -> SentinelConfigLoader -> ConfigUtil -> SentinelConfig
        // so not use SentinelConfig.charset()
        return Charset.forName(System.getProperty("csp.sentinel.charset", StandardCharsets.UTF_8.name()));
    }

    public static String addSeparator(String dir) {
        if (!dir.endsWith(File.separator)) {
            dir += File.separator;
        }
        return dir;
    }

    private ConfigUtil() {}
}
