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

    public static final String LOG_CONFIG = "csp.sentinel.config.file";

    private static final String DEFAULT_LOG_CONFIG_FILE = "classpath:sentinel.properties";

    private static final Properties properties = new Properties();

    static {
        load();
    }

    private static void load() {
        String file = System.getProperty(LOG_CONFIG);
        if (StringUtil.isBlank(file)) {
            file = DEFAULT_LOG_CONFIG_FILE;
        }

        Properties p = ConfigUtil.loadProperties(file);
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
