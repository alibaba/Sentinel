/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.dubbo.springboot.configuration;

import com.alibaba.csp.sentinel.adapter.dubbo.DubboUtils;
import com.alibaba.csp.sentinel.adapter.dubbo.config.DubboConfig;
import com.alibaba.csp.sentinel.adapter.dubbo.fallback.DubboFallback;
import com.alibaba.csp.sentinel.dubbo.springboot.utils.ResourceUtils;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.apache.dubbo.rpc.AsyncRpcResult;
import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * @author zhengzechao
 * @date 2020-02-15
 */

public class FallbackManager implements DubboFallback {

    /**
     * methodResourceName -> fallback method
     */
    private Map<String, Method> fallbackMethod = new ConcurrentHashMap<>();
    /**
     * Cached data for fallback impl supplier, keyed by interface resourcenames.
     */
    private Map<String, FallbackImplSupplier> fallbackImplMap = new ConcurrentHashMap<>();

    @Override
    public Result handle(Invoker<?> invoker, Invocation invocation, BlockException ex) {
        String methodResourceName = DubboUtils.getResourceName(invoker, invocation, DubboConfig.getDubboProviderPrefix());
        String interfaceResourceName = ResourceUtils.getInterfaceResourceName(invoker.getUrl());
        FallbackImplSupplier fallbackImplSupplier = fallbackImplMap.get(interfaceResourceName);
        Method method = fallbackMethod.get(methodResourceName);
        try {
            return AsyncRpcResult.newDefaultAsyncResult(method.invoke(fallbackImplSupplier.get(), invocation.getArguments()), invocation);
        } catch (Exception e) {
            return AsyncRpcResult.newDefaultAsyncResult(new IllegalStateException("Failed to trigger fallback method!", e), invocation);
        }
    }

    void setFallbackMethod(String resource, Method method) {
        fallbackMethod.put(resource, method);
    }

    void setFallbackImpl(String key, FallbackImplSupplier fallbackImplSupplier) {
        fallbackImplMap.put(key, fallbackImplSupplier);
    }

    public Map<String, Method> getFallbackMethod() {
        return fallbackMethod;
    }

    public Map<String, FallbackImplSupplier> getFallbackImplMap() {
        return fallbackImplMap;
    }

    static class FallbackImplSupplier implements Supplier<Object> {

        /**
         * the actual fallback implementation
         */
        private volatile Object actual;

        /**
         * the delegated supplier to get the actual fallback implementation
         */
        private Supplier delegate;

        FallbackImplSupplier(Supplier delegate) {
            this.delegate = delegate;
        }

        @Override
        public Object get() {
            if (actual == null) {
                synchronized (this) {
                    if (actual == null) {
                        actual = delegate.get();
                    }
                }
            }
            return actual;
        }
    }
}
