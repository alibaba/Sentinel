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
package com.alibaba.csp.sentinel.adapter.pigeon.fallback;

import com.alibaba.csp.sentinel.util.AssertUtil;

public final class PigeonFallbackRegistry {

    private static volatile PigeonFallback invokerFallback = new DefaultPigeonFallback();
    private static volatile PigeonFallback providerFallback = new DefaultPigeonFallback();

    public static PigeonFallback getInvokerFallback() {
        return invokerFallback;
    }

    public static void setInvokerFallback(PigeonFallback invokerFallback) {
        AssertUtil.notNull(invokerFallback, "invokerFallback cannot be null");
        PigeonFallbackRegistry.invokerFallback = invokerFallback;
    }

    public static PigeonFallback getProviderFallback() {
        return providerFallback;
    }

    public static void setProviderFallback(PigeonFallback providerFallback) {
        AssertUtil.notNull(providerFallback, "providerFallback cannot be null");
        PigeonFallbackRegistry.providerFallback = providerFallback;
    }

    private PigeonFallbackRegistry() {}

}
