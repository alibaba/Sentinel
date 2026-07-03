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
package com.alibaba.csp.sentinel.adapter.dubbo3.fallback;

import com.alibaba.csp.sentinel.adapter.dubbo3.config.DubboAdapterGlobalConfig;

/**
 * <p>Global fallback registry for Dubbo.</p>
 *
 * @author Eric Zhao
 * @deprecated use {@link DubboAdapterGlobalConfig} instead since 1.8.0.
 */
@Deprecated
public final class DubboFallbackRegistry {

    public static DubboFallback getConsumerFallback() {
        return DubboAdapterGlobalConfig.getConsumerFallback();
    }

    public static void setConsumerFallback(DubboFallback consumerFallback) {
        DubboAdapterGlobalConfig.setConsumerFallback(consumerFallback);
    }

    public static DubboFallback getProviderFallback() {
        return DubboAdapterGlobalConfig.getProviderFallback();
    }

    public static void setProviderFallback(DubboFallback providerFallback) {
        DubboAdapterGlobalConfig.setProviderFallback(providerFallback);
    }

    private DubboFallbackRegistry() {}
}
