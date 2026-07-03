/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.adapter.motan.config;

import com.alibaba.csp.sentinel.adapter.motan.fallback.DefaultMotanFallback;
import com.alibaba.csp.sentinel.adapter.motan.fallback.MotanFallback;
import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * @author zhangxn8
 */
public class MotanAdapterGlobalConfig {

    private static final String TRUE_STR = "true";

    public static final String APPLICATION = "application";
    public static final String MOTAN = "motan";

    public static final String BASE_SENTINEL_MOTAN_FILTER = "baseSentinelMotanFilter";
    public static final String MOTAN_APP_CONTEXT = "motanAppContext";
    public static final String SENTINEL_MOTAN_CONSUMER = "sentinelMotanConsumer";
    public static final String SENTINEL_MOTAN_PROVIDER = "sentinelMotanProvider";

    public static final String MOTAN_RES_NAME_WITH_PREFIX_KEY = "csp.sentinel.motan.resource.use.prefix";
    public static final String MOTAN_PROVIDER_RES_NAME_PREFIX_KEY = "csp.sentinel.motan.resource.provider.prefix";
    public static final String MOTAN_CONSUMER_RES_NAME_PREFIX_KEY = "csp.sentinel.motan.resource.consumer.prefix";

    public static final String MOTAN_INTERFACE_GROUP_VERSION_ENABLED = "csp.sentinel.motan.interface.group.version.enabled";

    private static final String DEFAULT_MOTAN_PROVIDER_PREFIX = "motan:provider:";
    private static final String DEFAULT_MOTAN_CONSUMER_PREFIX = "motan:consumer:";

    private static volatile MotanFallback consumerFallback = new DefaultMotanFallback();
    private static volatile MotanFallback providerFallback = new DefaultMotanFallback();

    private MotanAdapterGlobalConfig() {}

    public static boolean isUsePrefix() {
        return TRUE_STR.equalsIgnoreCase(SentinelConfig.getConfig(MOTAN_RES_NAME_WITH_PREFIX_KEY));
    }

    public static String getMotanProviderPrefix() {
        if (isUsePrefix()) {
            String config = SentinelConfig.getConfig(MOTAN_PROVIDER_RES_NAME_PREFIX_KEY);
            return StringUtil.isNotBlank(config) ? config : DEFAULT_MOTAN_PROVIDER_PREFIX;
        }
        return null;
    }

    public static String getMotanConsumerPrefix() {
        if (isUsePrefix()) {
            String config = SentinelConfig.getConfig(MOTAN_CONSUMER_RES_NAME_PREFIX_KEY);
            return StringUtil.isNotBlank(config) ? config : DEFAULT_MOTAN_CONSUMER_PREFIX;
        }
        return null;
    }

    public static Boolean getMotanInterfaceGroupAndVersionEnabled() {
        return TRUE_STR.equalsIgnoreCase(SentinelConfig.getConfig(MOTAN_INTERFACE_GROUP_VERSION_ENABLED));
    }

    public static MotanFallback getConsumerFallback() {
        return consumerFallback;
    }

    public static void setConsumerFallback(MotanFallback consumerFallback) {
        AssertUtil.notNull(consumerFallback, "consumerFallback cannot be null");
        MotanAdapterGlobalConfig.consumerFallback = consumerFallback;
    }

    public static MotanFallback getProviderFallback() {
        return providerFallback;
    }

    public static void setProviderFallback(MotanFallback providerFallback) {
        AssertUtil.notNull(providerFallback, "providerFallback cannot be null");
        MotanAdapterGlobalConfig.providerFallback = providerFallback;
    }
}
