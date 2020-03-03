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
package com.alibaba.csp.sentinel.adapter.sofa.rpc.fallback;

import com.alibaba.csp.sentinel.util.AssertUtil;

/**
 * Global Sentinel fallback registry for SOFARPC services.
 *
 * @author cdfive
 */
public final class SofaRpcFallbackRegistry {

    private static volatile SofaRpcFallback providerFallback = new DefaultSofaRpcFallback();
    private static volatile SofaRpcFallback consumerFallback = new DefaultSofaRpcFallback();

    public static SofaRpcFallback getProviderFallback() {
        return providerFallback;
    }

    public static void setProviderFallback(SofaRpcFallback providerFallback) {
        AssertUtil.notNull(providerFallback, "providerFallback cannot be null");
        SofaRpcFallbackRegistry.providerFallback = providerFallback;
    }

    public static SofaRpcFallback getConsumerFallback() {
        return consumerFallback;
    }

    public static void setConsumerFallback(SofaRpcFallback consumerFallback) {
        AssertUtil.notNull(consumerFallback, "consumerFallback cannot be null");
        SofaRpcFallbackRegistry.consumerFallback = consumerFallback;
    }

    private SofaRpcFallbackRegistry() {}
}

