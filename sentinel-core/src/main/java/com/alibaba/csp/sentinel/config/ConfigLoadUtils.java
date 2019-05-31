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
package com.alibaba.csp.sentinel.config;

import com.alibaba.csp.sentinel.util.AppNameUtil;
import com.alibaba.csp.sentinel.util.StringUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author lianglin
 * @since 2019-05-31 15:19
 */
public class ConfigLoadUtils {

    private static String DEFAULT_SENTINEL_CUSTOMIZE_FILE = "sentinel.properties";
    private static String SENTINEL_CONFIG_FILE = "csp.sentinel.config.file";

    private static final String DIR_NAME = "logs" + File.separator + "csp";
    private static final String USER_HOME = "user.home";

    private static Properties properties = new Properties();

    static {
        init();
    }

    public static Properties loadProperties() {
        return properties;
    }


    private static void init() {

        //Load from JVM -D user-specified file
        String fileName = System.getProperty(SENTINEL_CONFIG_FILE);
        if (StringUtil.isNotBlank(fileName)) {
            if (fileName.startsWith("/")) {
                loadPropertiesFromFille(fileName);
            } else {
                loadPropertiesFromResource(fileName);
            }
        } else {
            //try load from default file
            loadPropertiesFromResource(DEFAULT_SENTINEL_CUSTOMIZE_FILE);
            if (properties.isEmpty()) {
                //may be old version
                String userHome = System.getProperty(USER_HOME);
                String path = addSeparator(userHome) + DIR_NAME + File.separator;
                AppNameUtil.resolveAppName();
                String appName = AppNameUtil.getAppName();
                fileName = path + appName + ".properties";
                File file = new File(fileName);
                if (file.exists()) {
                    loadPropertiesFromFille(fileName);
                }
            }
        }
        //JVM parameter override file config
        CopyOnWriteArraySet<Map.Entry<Object, Object>> entries = new CopyOnWriteArraySet<>(System.getProperties().entrySet());
        for (Map.Entry entry : entries) {
            properties.setProperty(entry.getKey().toString(), entry.getValue().toString());
        }

    }

    private static void loadPropertiesFromFille(String fileName) {
        try {
            FileInputStream input = new FileInputStream(fileName);
            try {
                properties.load(input);
            } finally {
                input.close();
            }
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    private static void loadPropertiesFromResource(String fileName) {
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
    }

    private static ClassLoader getClassLoader() {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = ConfigLoadUtils.class.getClassLoader();
        }
        return classLoader;
    }

    private static String addSeparator(String logDir) {
        if (!logDir.endsWith(File.separator)) {
            logDir += File.separator;
        }
        return logDir;
    }


}
