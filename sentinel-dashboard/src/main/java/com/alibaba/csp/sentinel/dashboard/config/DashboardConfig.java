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
package com.alibaba.csp.sentinel.dashboard.config;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.lang.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * <p>Dashboard local config support.</p>
 * <p>
 * Dashboard supports configuration loading by several ways by order:<br>
 * 1. System.properties<br>
 * 2. Env
 * </p>
 *
 * @author jason
 * @since 1.5.0
 */
public class DashboardConfig {

    public static final int DEFAULT_MACHINE_HEALTHY_TIMEOUT_MS = 60_000;

    /**
     * Login username
     */
    public static final String CONFIG_AUTH_USERNAME = "sentinel.dashboard.auth.username";

    /**
     * Login password
     */
    public static final String CONFIG_AUTH_PASSWORD = "sentinel.dashboard.auth.password";

    /**
     * Hide application name in sidebar when it has no healthy machines after specific period in millisecond.
     */
    public static final String CONFIG_HIDE_APP_NO_MACHINE_MILLIS = "sentinel.dashboard.app.hideAppNoMachineMillis";
    /**
     * Remove application when it has no healthy machines after specific period in millisecond.
     */
    public static final String CONFIG_REMOVE_APP_NO_MACHINE_MILLIS = "sentinel.dashboard.removeAppNoMachineMillis";
    /**
     * Timeout
     */
    public static final String CONFIG_UNHEALTHY_MACHINE_MILLIS = "sentinel.dashboard.unhealthyMachineMillis";
    /**
     * Auto remove unhealthy machine after specific period in millisecond.
     */
    public static final String CONFIG_AUTO_REMOVE_MACHINE_MILLIS = "sentinel.dashboard.autoRemoveMachineMillis";

    private static final ConcurrentMap<String, Object> cacheMap = new ConcurrentHashMap<>();

    public static final String FLOW_RULE_TYPE = "flow";
    public static final String DEGRADE_RULE_TYPE = "degrade";
    public static final String SYSTEM_RULE_TYPE = "system";
    public static final String AUTHORITY_TYPE = "authority";
    public static final String PARAM_FLOW_TYPE = "paramFlow";
    public static final String API_DEFINITIONS_TYPE = "apiDefinitions";
    public static final String GATEWAY_FLOW_TYPE = "gatewayFlow";

    /**
     * Persistent flag of the third-party ,default false memory persistent
     */
    public static Boolean THIRD_PARTY_PERSISTENCE_FLAG = Boolean.FALSE;

    /**
     * Third-party persistence initializes memory the app/type list
     */
    public static final List<String> THIRD_PARTY_PERSISTENCE_LIST = Collections.synchronizedList(new ArrayList<>());

    private static final String CONCAT_APP_TYPE_TEMPLATE = "%s/%s";

    /**
     * @param app  appName
     * @param type type
     * @return String
     */
    public static String concatAppAndType(String app, String type) {
        return String.format(CONCAT_APP_TYPE_TEMPLATE, app, type);
    }

    /**
     * Initialization memory is required
     *
     * @param appType appType
     * @return Boolean
     */
    public static Boolean initFlag(String appType) {
        return THIRD_PARTY_PERSISTENCE_FLAG &&
                !THIRD_PARTY_PERSISTENCE_LIST.contains(appType);
    }


    @NonNull
    private static String getConfig(String name) {
        // env
        String val = System.getenv(name);
        if (StringUtils.isNotEmpty(val)) {
            return val;
        }
        // properties
        val = System.getProperty(name);
        if (StringUtils.isNotEmpty(val)) {
            return val;
        }
        return "";
    }

    protected static String getConfigStr(String name) {
        if (cacheMap.containsKey(name)) {
            return (String) cacheMap.get(name);
        }

        String val = getConfig(name);

        if (StringUtils.isBlank(val)) {
            return null;
        }

        cacheMap.put(name, val);
        return val;
    }

    protected static int getConfigInt(String name, int defaultVal, int minVal) {
        if (cacheMap.containsKey(name)) {
            return (int) cacheMap.get(name);
        }
        int val = NumberUtils.toInt(getConfig(name));
        if (val == 0) {
            val = defaultVal;
        } else if (val < minVal) {
            val = minVal;
        }
        cacheMap.put(name, val);
        return val;
    }

    public static String getAuthUsername() {
        return getConfigStr(CONFIG_AUTH_USERNAME);
    }

    public static String getAuthPassword() {
        return getConfigStr(CONFIG_AUTH_PASSWORD);
    }

    public static int getHideAppNoMachineMillis() {
        return getConfigInt(CONFIG_HIDE_APP_NO_MACHINE_MILLIS, 0, 60000);
    }

    public static int getRemoveAppNoMachineMillis() {
        return getConfigInt(CONFIG_REMOVE_APP_NO_MACHINE_MILLIS, 0, 120000);
    }

    public static int getAutoRemoveMachineMillis() {
        return getConfigInt(CONFIG_AUTO_REMOVE_MACHINE_MILLIS, 0, 300000);
    }

    public static int getUnhealthyMachineMillis() {
        return getConfigInt(CONFIG_UNHEALTHY_MACHINE_MILLIS, DEFAULT_MACHINE_HEALTHY_TIMEOUT_MS, 30000);
    }

    public static void clearCache() {
        cacheMap.clear();
    }
}
