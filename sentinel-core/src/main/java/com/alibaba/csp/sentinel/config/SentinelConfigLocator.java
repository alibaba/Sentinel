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

import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.AppNameUtil;
import com.alibaba.csp.sentinel.util.ConfigUtil;
import com.alibaba.csp.sentinel.util.StringUtil;

import java.io.File;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * <p>
 * class responsible for loading the Sentinel Core configuration
 * </p>
 *
 * @author lianglin
 * @since 1.7.0
 */
public class SentinelConfigLocator {


    private static final String DIR_NAME = "logs" + File.separator + "csp";
    private static final String USER_HOME = "user.home";

    private static String SENTINEL_CONFIG = "csp.sentinel.config.file";
    private static String DEFAULT_SENTINEL_CONFIG = "sentinel.properties";


    private static Properties properties = new Properties();

    static {
        locate();
    }


    private static void locate() {

        String fileName = System.getProperty(SENTINEL_CONFIG);
        if (StringUtil.isBlank(fileName)) {
            fileName = DEFAULT_SENTINEL_CONFIG;
        }

        Properties p = ConfigUtil.loadPropertiesFromFile(fileName);

        //old version config file
        if (p == null || p.isEmpty()) {
            String path = addSeparator(System.getProperty(USER_HOME)) + DIR_NAME + File.separator;
            fileName = path + AppNameUtil.getAppName() + ".properties";
            File file = new File(fileName);
            if (file.exists()) {
                properties.putAll(ConfigUtil.loadPropertiesFromFile(fileName));
            }
        } else {
            properties.putAll(p);
        }

        for (Map.Entry<Object, Object> entry : new CopyOnWriteArraySet<>(System.getProperties().entrySet())) {
            String configKey = entry.getKey().toString();
            String configValue = entry.getValue().toString();
            String configValueOld = properties.getProperty(configKey);
            properties.put(configKey, configValue);
            if (configValueOld != null) {
                RecordLog.info("[SentinelConfig] JVM parameter overrides {0}: {1} -> {2}", configKey, configValueOld, configValue);
            }
        }
    }

    private static String addSeparator(String logDir) {
        if (!logDir.endsWith(File.separator)) {
            logDir += File.separator;
        }
        return logDir;
    }

    public static Properties locateProperties() {
        return properties;
    }


}
