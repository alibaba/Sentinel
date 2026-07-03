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
package com.alibaba.csp.sentinel.log;

import com.alibaba.csp.sentinel.util.ConfigUtil;
import com.alibaba.csp.sentinel.util.StringUtil;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * <p>The loader that responsible for loading Sentinel log configurations.</p>
 *
 * @author lianglin
 * @since 1.7.0
 */
public class LogConfigLoader {

    public static final String LOG_CONFIG_ENV_KEY = "CSP_SENTINEL_CONFIG_FILE";
    public static final String LOG_CONFIG_PROPERTY_KEY = "csp.sentinel.config.file";

    private static final String DEFAULT_LOG_CONFIG_FILE = "classpath:sentinel.properties";

    private static final Properties properties = new Properties();

    static {
        try {
            load();
        } catch (Throwable t) {
            // NOTE: do not use RecordLog here, or there will be circular class dependency!
            System.err.println("[LogConfigLoader] Failed to initialize configuration items");
            t.printStackTrace();
        }
    }

    private static void load() {
        // Order: system property -> system env -> default file (classpath:sentinel.properties) -> legacy path
        String fileName = System.getProperty(LOG_CONFIG_PROPERTY_KEY);
        if (StringUtil.isBlank(fileName)) {
            fileName = System.getenv(LOG_CONFIG_ENV_KEY);
            if (StringUtil.isBlank(fileName)) {
                fileName = DEFAULT_LOG_CONFIG_FILE;
            }
        }

        Properties p = ConfigUtil.loadProperties(fileName);
        if (p != null && !p.isEmpty()) {
            properties.putAll(p);
        }

        CopyOnWriteArraySet<Map.Entry<Object, Object>> copy = new CopyOnWriteArraySet<>(System.getProperties().entrySet());
        for (Map.Entry<Object, Object> entry : copy) {
            String configKey = entry.getKey().toString();
            String newConfigValue = entry.getValue().toString();
            properties.put(configKey, newConfigValue);
        }
    }

    public static Properties getProperties() {
        return properties;
    }
}
