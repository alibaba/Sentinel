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

import static com.alibaba.csp.sentinel.util.ConfigUtil.addSeparator;

/**
 * <p>The loader that responsible for loading Sentinel common configurations.</p>
 *
 * @author lianglin
 * @since 1.7.0
 */
public final class SentinelConfigLoader {

    public static final String SENTINEL_CONFIG_ENV_KEY = "CSP_SENTINEL_CONFIG_FILE";
    public static final String SENTINEL_CONFIG_PROPERTY_KEY = "csp.sentinel.config.file";

    private static final String DEFAULT_SENTINEL_CONFIG_FILE = "classpath:sentinel.properties";

    private static Properties properties = new Properties();

    static {
        try {
            load();
        } catch (Throwable t) {
            RecordLog.warn("[SentinelConfigLoader] Failed to initialize configuration items", t);
        }
    }

    private static void load() {
        // Order: system property -> system env -> default file (classpath:sentinel.properties) -> legacy path
        String fileName = System.getProperty(SENTINEL_CONFIG_PROPERTY_KEY);
        if (StringUtil.isBlank(fileName)) {
            fileName = System.getenv(SENTINEL_CONFIG_ENV_KEY);
            if (StringUtil.isBlank(fileName)) {
                fileName = DEFAULT_SENTINEL_CONFIG_FILE;
            }
        }

        Properties p = ConfigUtil.loadProperties(fileName);
        if (p != null && !p.isEmpty()) {
            RecordLog.info("[SentinelConfigLoader] Loading Sentinel config from {}", fileName);
            properties.putAll(p);
        }

        for (Map.Entry<Object, Object> entry : new CopyOnWriteArraySet<>(System.getProperties().entrySet())) {
            String configKey = entry.getKey().toString();
            String newConfigValue = entry.getValue().toString();
            String oldConfigValue = properties.getProperty(configKey);
            properties.put(configKey, newConfigValue);
            if (oldConfigValue != null) {
                RecordLog.info("[SentinelConfigLoader] JVM parameter overrides {}: {} -> {}",
                        configKey, oldConfigValue, newConfigValue);
            }
        }
    }


    public static Properties getProperties() {
        return properties;
    }

}
