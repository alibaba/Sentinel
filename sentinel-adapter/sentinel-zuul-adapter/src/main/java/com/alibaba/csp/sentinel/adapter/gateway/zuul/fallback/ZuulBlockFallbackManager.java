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

package com.alibaba.csp.sentinel.adapter.gateway.zuul.fallback;

import com.alibaba.csp.sentinel.util.AssertUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * This provide fall back class manager.
 *
 * @author tiger
 */
public class ZuulBlockFallbackManager {

    private static final Map<String, ZuulBlockFallbackProvider<?extends BlockResponse>> FALLBACK_PROVIDER_CACHE = new HashMap<>();

    private static ZuulBlockFallbackProvider<? extends BlockResponse> defaultFallbackProvider = new DefaultBlockFallbackProvider();

    /**
     * Register special provider for different route.
     */
    public static synchronized <T> void registerProvider(ZuulBlockFallbackProvider<? super T> provider) {
        AssertUtil.notNull(provider, "fallback provider cannot be null");
        String route = provider.getRoute();
        String defaultRoute = "*";
        if (defaultRoute.equals(route) || route == null) {
            defaultFallbackProvider = provider;
        } else {
            FALLBACK_PROVIDER_CACHE.put(route, provider);
        }
    }

    public static ZuulBlockFallbackProvider<? extends BlockResponse> getFallbackProvider(String route) {
        ZuulBlockFallbackProvider<?extends BlockResponse> provider = FALLBACK_PROVIDER_CACHE.get(route);
        if (provider == null) {
            provider = defaultFallbackProvider;
        }
        return provider;
    }

    public synchronized static void clear(){
        FALLBACK_PROVIDER_CACHE.clear();
    }

}
