package com.alibaba.csp.sentinel.metric.prom.config;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.util.StringUtil;

public class PrometheusGlobalConfig {

    public static final String PROM_FETCH_SIZE = "csp.sentinel.prometheus.fetch.size";
    public static final String DEFAULT_PROM_FETCH_SIZE = "1024";

    public static final String PROM_FETCH_DELAY = "csp.sentinel.prometheus.fetch.delay";
    public static final String DEFAULT_PROM_FETCH_DELAY = "0";

    public static final String PROM_FETCH_IDENTIFY = "csp.sentinel.prometheus.fetch.identify";

    public static final String PROM_FETCH_TYPES = "csp.sentinel.prometheus.fetch.types";
    public static final String DEFAULT_PROM_FETCH_TYPES = "passQps|blockQps|exceptionQps|rt|concurrency";

    public static final String PROM_APP = "csp.sentinel.prometheus.app";
    public static final String DEFAULT_PROM_APP = "SENTINEL_APP";

    public static int getPromFetchSize() {
        String config = SentinelConfig.getConfig(PROM_FETCH_SIZE);
        config = StringUtil.isNotBlank(config) ? config : DEFAULT_PROM_FETCH_SIZE;
        return Integer.parseInt(config);
    }

    public static int getPromFetchDelayTime() {
        String config = SentinelConfig.getConfig(PROM_FETCH_DELAY);
        config = StringUtil.isNotBlank(config) ? config : DEFAULT_PROM_FETCH_DELAY;
        return Integer.parseInt(config);
    }

    public static String getPromFetchIdentify() {
        return SentinelConfig.getConfig(PROM_FETCH_IDENTIFY);
    }

    public static String[] getPromFetchTypes() {
        String config = SentinelConfig.getConfig(PROM_FETCH_TYPES);
        config = StringUtil.isNotBlank(config) ? config : DEFAULT_PROM_FETCH_TYPES;
        try {
            return config.split("\\|");
        }catch (Throwable e){
            return DEFAULT_PROM_FETCH_TYPES.split("\\|");
        }
    }

    public static String getPromFetchApp() {
        String appName = SentinelConfig.getConfig(PROM_APP);
        if (appName == null) {
            appName = SentinelConfig.getAppName();
        }

        if (appName == null) {
            appName = DEFAULT_PROM_APP;
        }
        appName = appName.replaceAll("\\.","_");
        return appName;
    }

}
