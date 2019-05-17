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
import java.util.concurrent.CopyOnWriteArraySet;

import com.alibaba.csp.sentinel.log.LogBase;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.AppNameUtil;
import com.alibaba.csp.sentinel.util.AssertUtil;

/**
 * The universal local config center of Sentinel. The config is retrieved from command line arguments
 * and {@code ${user.home}/logs/csp/${appName}.properties} file by default.
 *
 * @author leyou
 * @author Eric Zhao
 */
public class SentinelConfig {

    /**
     * The default application type.
     *
     * @since 1.6.0
     */
    public static final int APP_TYPE_COMMON = 0;

    private static final Map<String, String> props = new ConcurrentHashMap<>();
    private static int appType = APP_TYPE_COMMON;

    public static final String APP_TYPE = "csp.sentinel.app.type";
    public static final String CHARSET = "csp.sentinel.charset";
    public static final String SINGLE_METRIC_FILE_SIZE = "csp.sentinel.metric.file.single.size";
    public static final String TOTAL_METRIC_FILE_COUNT = "csp.sentinel.metric.file.total.count";
    public static final String COLD_FACTOR = "csp.sentinel.flow.cold.factor";
    public static final String STATISTIC_MAX_RT = "csp.sentinel.statistic.max.rt";

    static final String DEFAULT_CHARSET = "UTF-8";
    static final long DEFAULT_SINGLE_METRIC_FILE_SIZE = 1024 * 1024 * 50;
    static final int DEFAULT_TOTAL_METRIC_FILE_COUNT = 6;
    static final int DEFAULT_COLD_FACTOR = 3;
    static final int DEFAULT_STATISTIC_MAX_RT = 4900;

    static {
        try {
            initialize();
            loadProps();

            resolveAppType();
            RecordLog.info("[SentinelConfig] Application type resolved: " + appType);
        } catch (Throwable ex) {
            RecordLog.warn("[SentinelConfig] Failed to initialize", ex);
            ex.printStackTrace();
        }
    }

    private static void resolveAppType() {
        try {
            String type = getConfig(APP_TYPE);
            if (type == null) {
                appType = APP_TYPE_COMMON;
                return;
            }
            appType = Integer.parseInt(type);
            if (appType < 0) {
                appType = APP_TYPE_COMMON;
            }
        } catch (Exception ex) {
            appType = APP_TYPE_COMMON;
        }
    }

    private static void initialize() {
        // Init default properties.
        SentinelConfig.setConfig(CHARSET, DEFAULT_CHARSET);
        SentinelConfig.setConfig(SINGLE_METRIC_FILE_SIZE, String.valueOf(DEFAULT_SINGLE_METRIC_FILE_SIZE));
        SentinelConfig.setConfig(TOTAL_METRIC_FILE_COUNT, String.valueOf(DEFAULT_TOTAL_METRIC_FILE_COUNT));
        SentinelConfig.setConfig(COLD_FACTOR, String.valueOf(DEFAULT_COLD_FACTOR));
        SentinelConfig.setConfig(STATISTIC_MAX_RT, String.valueOf(DEFAULT_STATISTIC_MAX_RT));
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
        for (Map.Entry<Object, Object> entry : new CopyOnWriteArraySet<>(System.getProperties().entrySet())) {
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
        AssertUtil.notNull(key, "key cannot be null");
        return props.get(key);
    }

    public static void setConfig(String key, String value) {
        AssertUtil.notNull(key, "key cannot be null");
        AssertUtil.notNull(value, "value cannot be null");
        props.put(key, value);
    }

    public static String removeConfig(String key) {
        AssertUtil.notNull(key, "key cannot be null");
        return props.remove(key);
    }

    public static void setConfigIfAbsent(String key, String value) {
        AssertUtil.notNull(key, "key cannot be null");
        AssertUtil.notNull(value, "value cannot be null");
        String v = props.get(key);
        if (v == null) {
            props.put(key, value);
        }
    }

    public static String getAppName() {
        return AppNameUtil.getAppName();
    }

    /**
     * Get application type.
     *
     * @return application type, common (0) by default
     * @since 1.6.0
     */
    public static int getAppType() {
        return appType;
    }

    public static String charset() {
        return props.get(CHARSET);
    }

    public static long singleMetricFileSize() {
        try {
            return Long.parseLong(props.get(SINGLE_METRIC_FILE_SIZE));
        } catch (Throwable throwable) {
            RecordLog.warn("[SentinelConfig] Parse singleMetricFileSize fail, use default value: "
                + DEFAULT_SINGLE_METRIC_FILE_SIZE, throwable);
            return DEFAULT_SINGLE_METRIC_FILE_SIZE;
        }
    }

    public static int totalMetricFileCount() {
        try {
            return Integer.parseInt(props.get(TOTAL_METRIC_FILE_COUNT));
        } catch (Throwable throwable) {
            RecordLog.warn("[SentinelConfig] Parse totalMetricFileCount fail, use default value: "
                + DEFAULT_TOTAL_METRIC_FILE_COUNT, throwable);
            return DEFAULT_TOTAL_METRIC_FILE_COUNT;
        }
    }

    public static int coldFactor() {
        try {
            int coldFactor = Integer.parseInt(props.get(COLD_FACTOR));
            // check the cold factor larger than 1
            if (coldFactor <= 1) {
                coldFactor = DEFAULT_COLD_FACTOR;
                RecordLog.warn("cold factor=" + coldFactor + ", should be larger than 1, use default value: "
                        + DEFAULT_COLD_FACTOR);
            }
            return coldFactor;
        } catch (Throwable throwable) {
            RecordLog.warn("[SentinelConfig] Parse coldFactor fail, use default value: "
                    + DEFAULT_COLD_FACTOR, throwable);
            return DEFAULT_COLD_FACTOR;
        }
    }

    public static int statisticMaxRt() {
        try {
            return Integer.parseInt(props.get(STATISTIC_MAX_RT));
        } catch (Throwable throwable) {
            RecordLog.warn("[SentinelConfig] Parse statisticMaxRt fail, use default value: "
                    + DEFAULT_STATISTIC_MAX_RT, throwable);
            return DEFAULT_STATISTIC_MAX_RT;
        }
    }
}
