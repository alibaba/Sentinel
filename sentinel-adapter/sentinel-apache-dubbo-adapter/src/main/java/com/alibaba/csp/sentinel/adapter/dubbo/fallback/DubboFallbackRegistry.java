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
package com.alibaba.csp.sentinel.adapter.dubbo.fallback;

import com.alibaba.csp.sentinel.util.AssertUtil;

/**
 * <p>Global fallback registry for Dubbo.</p>
 *
 * <p>
 * Note: Circuit breaking is mainly designed for consumer. The provider should not
 * give fallback result in most circumstances.
 * </p>
 *
 * @author Eric Zhao
 */
public final class DubboFallbackRegistry {

    private static volatile DubboFallback consumerFallback = new DefaultDubboFallback();
    private static volatile DubboFallback providerFallback = new DefaultDubboFallback();

    public static DubboFallback getConsumerFallback() {
        return consumerFallback;
    }

    public static void setConsumerFallback(DubboFallback consumerFallback) {
        AssertUtil.notNull(consumerFallback, "consumerFallback cannot be null");
        DubboFallbackRegistry.consumerFallback = consumerFallback;
    }

    public static DubboFallback getProviderFallback() {
        return providerFallback;
    }

    public static void setProviderFallback(DubboFallback providerFallback) {
        AssertUtil.notNull(providerFallback, "providerFallback cannot be null");
        DubboFallbackRegistry.providerFallback = providerFallback;
    }

    private DubboFallbackRegistry() {}
}
