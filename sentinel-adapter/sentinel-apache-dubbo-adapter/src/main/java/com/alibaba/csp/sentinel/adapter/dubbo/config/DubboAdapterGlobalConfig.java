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

import com.alibaba.csp.sentinel.adapter.dubbo.fallback.DefaultDubboFallback;
import com.alibaba.csp.sentinel.adapter.dubbo.fallback.DubboFallback;
import com.alibaba.csp.sentinel.adapter.dubbo.origin.DefaultDubboOriginParser;
import com.alibaba.csp.sentinel.adapter.dubbo.origin.DubboOriginParser;
import com.alibaba.csp.sentinel.config.SentinelConfig;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;

/**
 * <p>
 * Responsible for dubbo service provider, consumer attribute configuration
 * </p>
 *
 * @author lianglin
 * @since 1.7.0
 */
public final class DubboAdapterGlobalConfig {

    private static final String TRUE_STR = "true";

    public static final String DUBBO_RES_NAME_WITH_PREFIX_KEY = "csp.sentinel.dubbo.resource.use.prefix";
    public static final String DUBBO_PROVIDER_RES_NAME_PREFIX_KEY = "csp.sentinel.dubbo.resource.provider.prefix";
    public static final String DUBBO_CONSUMER_RES_NAME_PREFIX_KEY = "csp.sentinel.dubbo.resource.consumer.prefix";

    private static final String DEFAULT_DUBBO_PROVIDER_PREFIX = "dubbo:provider:";
    private static final String DEFAULT_DUBBO_CONSUMER_PREFIX = "dubbo:consumer:";

    public static final String DUBBO_INTERFACE_GROUP_VERSION_ENABLED = "csp.sentinel.dubbo.interface.group.version.enabled";

    private static volatile DubboFallback consumerFallback = new DefaultDubboFallback();
    private static volatile DubboFallback providerFallback = new DefaultDubboFallback();
    private static volatile DubboOriginParser originParser = new DefaultDubboOriginParser();

    public static boolean isUsePrefix() {
        return TRUE_STR.equalsIgnoreCase(SentinelConfig.getConfig(DUBBO_RES_NAME_WITH_PREFIX_KEY));
    }

    public static String getDubboProviderResNamePrefixKey() {
        if (isUsePrefix()) {
            String config = SentinelConfig.getConfig(DUBBO_PROVIDER_RES_NAME_PREFIX_KEY);
            return StringUtil.isNotBlank(config) ? config : DEFAULT_DUBBO_PROVIDER_PREFIX;
        }
        return null;
    }

    public static String getDubboConsumerResNamePrefixKey() {
        if (isUsePrefix()) {
            String config = SentinelConfig.getConfig(DUBBO_CONSUMER_RES_NAME_PREFIX_KEY);
            return StringUtil.isNotBlank(config) ? config : DEFAULT_DUBBO_CONSUMER_PREFIX;
        }
        return null;
    }

    public static Boolean getDubboInterfaceGroupAndVersionEnabled() {
        return TRUE_STR.equalsIgnoreCase(SentinelConfig.getConfig(DUBBO_INTERFACE_GROUP_VERSION_ENABLED));
    }

    public static DubboFallback getConsumerFallback() {
        return consumerFallback;
    }

    public static void setConsumerFallback(DubboFallback consumerFallback) {
        AssertUtil.notNull(consumerFallback, "consumerFallback cannot be null");
        DubboAdapterGlobalConfig.consumerFallback = consumerFallback;
    }

    public static DubboFallback getProviderFallback() {
        return providerFallback;
    }

    public static void setProviderFallback(DubboFallback providerFallback) {
        AssertUtil.notNull(providerFallback, "providerFallback cannot be null");
        DubboAdapterGlobalConfig.providerFallback = providerFallback;
    }

    /**
     * Get the origin parser of Dubbo adapter.
     *
     * @return the origin parser
     * @since 1.8.0
     */
    public static DubboOriginParser getOriginParser() {
        return originParser;
    }

    /**
     * Set the origin parser of Dubbo adapter.
     *
     * @param originParser the origin parser
     * @since 1.8.0
     */
    public static void setOriginParser(DubboOriginParser originParser) {
        AssertUtil.notNull(originParser, "originParser cannot be null");
        DubboAdapterGlobalConfig.originParser = originParser;
    }

    private DubboAdapterGlobalConfig() {}

}
