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
package com.alibaba.csp.sentinel.adapter.dubbo.config;

import com.alibaba.csp.sentinel.config.SentinelConfig;

/**
 * <p>
 * Responsible for dubbo service provider, consumer attribute configuration
 * </p>
 *
 * @author lianglin
 * @since 1.7.0
 */
public final class DubboConfig {

    public static final String DUBBO_USE_PREFIX = "csp.sentinel.dubbo.resource.use.prefix";
    private static boolean usePrefix;
    private static final String TRUE_STR = "true";

    public static final String DUBBO_PROVIDER_PREFIX = "csp.sentinel.dubbo.resource.provider.prefix";
    public static final String DUBBO_CONSUMER_PREFIX = "csp.sentinel.dubbo.resource.consumer.prefix";

    private static final String DEFAULT_DUBBO_PROVIDER_PREFIX = "dubbo:provider:";
    private static final String DEFAULT_DUBBO_CONSUMER_PREFIX = "dubbo:consumer:";

    static {
        String dubboUsePrefix = SentinelConfig.getConfig(DUBBO_USE_PREFIX);
        if (TRUE_STR.equalsIgnoreCase(dubboUsePrefix)) {
            usePrefix = true;
        } else {
            usePrefix = false;
        }
    }


    public static String getDubboProviderPrefix() {
        if (usePrefix) {
            String config = SentinelConfig.getConfig(DUBBO_PROVIDER_PREFIX);
            return config != null ? config : DEFAULT_DUBBO_PROVIDER_PREFIX;
        }
        return null;
    }

    public static String getDubboConsumerPrefix() {
        if (usePrefix) {
            String config = SentinelConfig.getConfig(DUBBO_CONSUMER_PREFIX);
            return config != null ? config : DEFAULT_DUBBO_CONSUMER_PREFIX;
        }
        return null;
    }


}
