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
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;

import java.io.File;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The universal local configuration center of Sentinel. The config is retrieved from command line arguments
 * and customized properties file by default.
 *
 * @author leyou
 * @author Eric Zhao
 * @author Lin Liang
 */
public final class SentinelConfig {

    /**
     * The default application type.
     *
     * @since 1.6.0
     */
    public static final int APP_TYPE_COMMON = 0;

    /**
     * Parameter value for using context classloader.
     */
    private static final String CLASSLOADER_CONTEXT = "context";

    private static final Map<String, String> props = new ConcurrentHashMap<>();

    private static int appType = APP_TYPE_COMMON;
    private static String appName = "";

    public static final String PROJECT_NAME_PROP_KEY = "project.name";
    public static final String APP_NAME_PROP_KEY = "csp.sentinel.app.name";
    public static final String APP_TYPE_PROP_KEY = "csp.sentinel.app.type";
    public static final String CHARSET = "csp.sentinel.charset";
    public static final String SINGLE_METRIC_FILE_SIZE = "csp.sentinel.metric.file.single.size";
    public static final String TOTAL_METRIC_FILE_COUNT = "csp.sentinel.metric.file.total.count";
    public static final String COLD_FACTOR = "csp.sentinel.flow.cold.factor";
    public static final String STATISTIC_MAX_RT = "csp.sentinel.statistic.max.rt";
    public static final String SPI_CLASSLOADER = "csp.sentinel.spi.classloader";
    public static final String METRIC_FLUSH_INTERVAL = "csp.sentinel.metric.flush.interval";

    public static final String DEFAULT_CHARSET = "UTF-8";
    public static final long DEFAULT_SINGLE_METRIC_FILE_SIZE = 1024 * 1024 * 50;
    public static final int DEFAULT_TOTAL_METRIC_FILE_COUNT = 6;
    public static final int DEFAULT_COLD_FACTOR = 3;
    public static final int DEFAULT_STATISTIC_MAX_RT = 5000;
    public static final long DEFAULT_METRIC_FLUSH_INTERVAL = 1L;

    static {
        try {
            initialize();
            loadProps();
            resolveAppName();
            resolveAppType();
            RecordLog.info("[SentinelConfig] Application type resolved: {}", appType);
        } catch (Throwable ex) {
            RecordLog.warn("[SentinelConfig] Failed to initialize", ex);
            ex.printStackTrace();
        }
    }

    private static void resolveAppType() {
        try {
            String type = getConfig(APP_TYPE_PROP_KEY);
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
        setConfig(CHARSET, DEFAULT_CHARSET);
        setConfig(SINGLE_METRIC_FILE_SIZE, String.valueOf(DEFAULT_SINGLE_METRIC_FILE_SIZE));
        setConfig(TOTAL_METRIC_FILE_COUNT, String.valueOf(DEFAULT_TOTAL_METRIC_FILE_COUNT));
        setConfig(COLD_FACTOR, String.valueOf(DEFAULT_COLD_FACTOR));
        setConfig(STATISTIC_MAX_RT, String.valueOf(DEFAULT_STATISTIC_MAX_RT));
        setConfig(METRIC_FLUSH_INTERVAL, String.valueOf(DEFAULT_METRIC_FLUSH_INTERVAL));
    }

    private static void loadProps() {
        Properties properties = SentinelConfigLoader.getProperties();
        for (Object key : properties.keySet()) {
            setConfig((String) key, (String) properties.get(key));
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
        return appName;
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
    
    /**
     * Get the metric log flush interval in second
     * @return  the metric log flush interval in second
     * @since 1.8.1
     */
    public static long metricLogFlushIntervalSec() {
        String flushIntervalStr = SentinelConfig.getConfig(METRIC_FLUSH_INTERVAL);
        if (flushIntervalStr == null) {
            return DEFAULT_METRIC_FLUSH_INTERVAL;
        }
        try {
            return Long.parseLong(flushIntervalStr);
        } catch (Throwable throwable) {
            RecordLog.warn("[SentinelConfig] Parse the metricLogFlushInterval fail, use default value: "
                    + DEFAULT_METRIC_FLUSH_INTERVAL, throwable);
            return DEFAULT_METRIC_FLUSH_INTERVAL;
        }
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

    /**
     * <p>Get the max RT value that Sentinel could accept for system BBR strategy.</p>
     *
     * @return the max allowed RT value
     * @since 1.4.1
     */
    public static int statisticMaxRt() {
        String v = props.get(STATISTIC_MAX_RT);
        try {
            if (StringUtil.isEmpty(v)) {
                return DEFAULT_STATISTIC_MAX_RT;
            }
            return Integer.parseInt(v);
        } catch (Throwable throwable) {
            RecordLog.warn("[SentinelConfig] Invalid statisticMaxRt value: {}, using the default value instead: "
                    + DEFAULT_STATISTIC_MAX_RT, v, throwable);
            SentinelConfig.setConfig(STATISTIC_MAX_RT, String.valueOf(DEFAULT_STATISTIC_MAX_RT));
            return DEFAULT_STATISTIC_MAX_RT;
        }
    }

    /**
     * Function for resolving project name. The order is elaborated below:
     *
     * <ol>
     * <li>Resolve the value from {@code CSP_SENTINEL_APP_NAME} system environment;</li>
     * <li>Resolve the value from {@code csp.sentinel.app.name} system property;</li>
     * <li>Resolve the value from {@code project.name} system property (for compatibility);</li>
     * <li>Resolve the value from {@code sun.java.command} system property, then remove path, arguments and ".jar" or ".JAR"
     * suffix, use the result as app name. Note that whitespace in file name or path is not allowed, or a
     * wrong app name may be gotten, For example:
     * <p>
     * <code>
     * "test.Main" -> test.Main<br/>
     * "/target/test.Main" -> test.Main<br/>
     * "/target/test.Main args1" -> test.Main<br/>
     * "Main.jar" -> Main<br/>
     * "/target/Main.JAR args1" -> Main<br/>
     * "Mai n.jar" -> Mai // whitespace in file name is not allowed<br/>
     * </code>
     * </p>
     * </li>
     * </ol>
     */
    private static void resolveAppName() {
        // Priority: system env -> csp.sentinel.app.name -> project.name -> main class (or jar) name
        String envKey = toEnvKey(APP_NAME_PROP_KEY);
        String n = System.getenv(envKey);
        if (!StringUtil.isBlank(n)) {
            appName = n;
            RecordLog.info("App name resolved from system env {}: {}", envKey, appName);
            return;
        }
        n = props.get(APP_NAME_PROP_KEY);
        if (!StringUtil.isBlank(n)) {
            appName = n;
            RecordLog.info("App name resolved from property {}: {}", APP_NAME_PROP_KEY, appName);
            return;
        }
        n = props.get(PROJECT_NAME_PROP_KEY);
        if (!StringUtil.isBlank(n)) {
            appName = n;
            RecordLog.info("App name resolved from property {}: {}", PROJECT_NAME_PROP_KEY, appName);
            return;
        }
        // Parse sun.java.command property by default.
        String command = System.getProperty("sun.java.command");
        if (StringUtil.isBlank(command)) {
            RecordLog.warn("Cannot resolve default appName from property sun.java.command");
            return;
        }
        command = command.split("\\s")[0];
        String separator = File.separator;
        if (command.contains(separator)) {
            String[] strs;
            if ("\\".equals(separator)) {
                // Handle separator in Windows.
                strs = command.split("\\\\");
            } else {
                strs = command.split(separator);
            }
            command = strs[strs.length - 1];
        }
        if (command.toLowerCase().endsWith(".jar")) {
            command = command.substring(0, command.length() - 4);
        }
        appName = command;
        RecordLog.info("App name resolved from default: {}", appName);
    }

    private static String toEnvKey(/*@NotBlank*/ String propKey) {
        return propKey.toUpperCase().replace('.', '_');
    }
    /**
     * Whether use context classloader via config parameter
     *
     * @return Whether use context classloader
     */
    public static boolean shouldUseContextClassloader() {
        String classloaderConf = SentinelConfig.getConfig(SentinelConfig.SPI_CLASSLOADER);
        return CLASSLOADER_CONTEXT.equalsIgnoreCase(classloaderConf);
    }

    private SentinelConfig() {}
}
