package com.alibaba.csp.sentinel.log;

import com.alibaba.csp.sentinel.util.ConfigUtil;
import com.alibaba.csp.sentinel.util.StringUtil;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * <p>
 * class responsible for loading the Log configuration.
 * </p>
 *
 * @author lianglin
 * @since 1.7.0
 */
public class LogConfigLocator {


    private static final String LOG_CONFIG = "csp.sentinel.config.file";

    private static final String DEFAULT_LOG_CONFIG_FILE = "sentinel.properties";


    private static final Properties properties = new Properties();

    static {
        locate();
    }

    private static void locate() {

        String file = System.getProperty(LOG_CONFIG);
        if (StringUtil.isBlank(file)) {
            file = DEFAULT_LOG_CONFIG_FILE;
        }

        Properties p = ConfigUtil.loadPropertiesFromFile(file);
        if (p != null && !p.isEmpty()) {
            properties.putAll(p);
        }

        CopyOnWriteArraySet<Map.Entry<Object, Object>> copy = new CopyOnWriteArraySet<>(System.getProperties().entrySet());
        for (Map.Entry<Object, Object> entry : copy) {
            String configKey = entry.getKey().toString();
            String newConfigValue = entry.getValue().toString();
            String oldConfigValue = properties.getProperty(configKey);
            properties.put(configKey, newConfigValue);
            if (oldConfigValue != null) {
                System.out.println("[LogConfig] JVM parameter overrides: " + configKey + " " + oldConfigValue + " -> " + newConfigValue);
            }
        }


    }

    public static Properties locateProperties() {
        return properties;
    }



}
