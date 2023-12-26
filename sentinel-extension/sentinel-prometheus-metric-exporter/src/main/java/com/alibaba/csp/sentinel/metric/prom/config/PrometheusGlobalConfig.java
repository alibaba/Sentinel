/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.metric.prom.config;

import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * The config for prometheus exporter.
 *
 * @author karl-sy
 * @date 2023-07-13 21:15
 * @since 2.0.0
 */
public class PrometheusGlobalConfig {

    public static final String PROM_FETCH_PORT = "csp.sentinel.prometheus.fetch.port";
    public static final String DEFAULT_PROM_FETCH_PORT = "9092";

    public static final String PROM_FETCH_SIZE = "csp.sentinel.prometheus.fetch.size";
    public static final String DEFAULT_PROM_FETCH_SIZE = "1024";

    public static final String PROM_FETCH_DELAY = "csp.sentinel.prometheus.fetch.delay";
    public static final String DEFAULT_PROM_FETCH_DELAY = "0";

    public static final String PROM_FETCH_IDENTIFY = "csp.sentinel.prometheus.fetch.identify";

    public static final String PROM_FETCH_TYPES = "csp.sentinel.prometheus.fetch.types";
    public static final String DEFAULT_PROM_FETCH_TYPES = "passQps|blockQps|exceptionQps|rt|concurrency";

    public static final String PROM_APP = "csp.sentinel.prometheus.app";
    public static final String DEFAULT_PROM_APP = "SENTINEL_APP";

    public static int getPromFetchPort() {
        String config = SentinelConfig.getConfig(PROM_FETCH_PORT);
        config = StringUtil.isNotBlank(config) ? config : DEFAULT_PROM_FETCH_PORT;
        return Integer.parseInt(config);
    }

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
        appName = appName.replaceAll("-","_");
        return appName;
    }

}
