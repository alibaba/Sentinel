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

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

import com.alibaba.csp.sentinel.log.LogBase;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.AppNameUtil;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * The universal config of Courier. The config is retrieved from
 * {@code ${user.home}/logs/csp/${appName}.properties} by default.
 *
 * @author leyou
 */
public class SentinelConfig {

    private static final Map<String, String> props = new ConcurrentHashMap<String, String>();

    public static final String CHARSET = "csp.sentinel.charset";
    public static final String SINGLE_METRIC_FILE_SIZE = "csp.sentinel.metric.file.single.size";
    public static final String TOTAL_METRIC_FILE_COUNT = "csp.sentinel.metric.file.total.count";
    public static final String COLD_FACTOR = "csp.sentinel.flow.cold.factor";

    static final long DEFAULT_SINGLE_METRIC_FILE_SIZE = 1024 * 1024 * 50;
    static final int DEFAULT_TOTAL_METRIC_FILE_COUNT = 6;

    static {
        initialize();
        loadProps();
    }

    private static void initialize() {
        // Init default properties.
        SentinelConfig.setConfig(CHARSET, "UTF-8");
        SentinelConfig.setConfig(SINGLE_METRIC_FILE_SIZE, String.valueOf(DEFAULT_SINGLE_METRIC_FILE_SIZE));
        SentinelConfig.setConfig(TOTAL_METRIC_FILE_COUNT, String.valueOf(DEFAULT_TOTAL_METRIC_FILE_COUNT));
        SentinelConfig.setConfig(COLD_FACTOR, String.valueOf(3));
    }

    private static void loadProps() {
        // Resolve app name.
        AppNameUtil.resolveAppName();
        try {
            String appName = AppNameUtil.getAppName();
            if (appName == null) {
                appName = "";
            }
            // We first retrieve the properties from the property file.
            String fileName = LogBase.getLogBaseDir() + appName + ".properties";
            File file = new File(fileName);
            if (file.exists()) {
                RecordLog.info("[SentinelConfig] Reading config from " + fileName);
                FileInputStream fis = new FileInputStream(fileName);
                Properties fileProps = new Properties();
                fileProps.load(fis);
                fis.close();

                for (Object key : fileProps.keySet()) {
                    SentinelConfig.setConfig((String)key, (String)fileProps.get(key));
                }
            }
        } catch (Throwable ioe) {
            RecordLog.info(ioe.getMessage(), ioe);
        }

        // JVM parameter override file config.
        for (Map.Entry<Object, Object> entry : System.getProperties().entrySet()) {
            String configKey = entry.getKey().toString();
            String configValue = entry.getValue().toString();
            String configValueOld = getConfig(configKey);
            SentinelConfig.setConfig(configKey, configValue);
            if (configValueOld != null) {
                RecordLog.info("[SentinelConfig] JVM parameter overrides {0}: {1} -> {2}", configKey, configValueOld, configValue);
            }
        }
    }

    /**
     * Get config value of the specific key.
     *
     * @param key config key
     * @return the config value.
     */
    public static String getConfig(String key) {
        return props.get(key);
    }

    public static void setConfig(String key, String value) {
        props.put(key, value);
    }

    public static void setConfigIfAbsent(String key, String value) {
        String v = props.get(key);
        if (v == null) {
            props.put(key, value);
        }
    }

    public static String getAppName() {
        return AppNameUtil.getAppName();
    }

    public static String charset() {
        return props.get(CHARSET);
    }

    public static long singleMetricFileSize() {
        try {
            return Long.parseLong(props.get(SINGLE_METRIC_FILE_SIZE));
        } catch (Throwable throwable) {
            RecordLog.info("[SentinelConfig] Parse singleMetricFileSize fail, use default value: "
                + DEFAULT_SINGLE_METRIC_FILE_SIZE, throwable);
            return DEFAULT_SINGLE_METRIC_FILE_SIZE;
        }
    }

    public static int totalMetricFileCount() {
        try {
            return Integer.parseInt(props.get(TOTAL_METRIC_FILE_COUNT));
        } catch (Throwable throwable) {
            RecordLog.info("[SentinelConfig] Parse totalMetricFileCount fail, use default value: "
                + DEFAULT_TOTAL_METRIC_FILE_COUNT, throwable);
            return DEFAULT_TOTAL_METRIC_FILE_COUNT;
        }
    }
}
