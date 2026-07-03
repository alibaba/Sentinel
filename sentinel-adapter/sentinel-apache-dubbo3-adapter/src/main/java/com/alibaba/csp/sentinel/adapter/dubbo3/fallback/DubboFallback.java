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

import com.alibaba.csp.sentinel.slots.block.BlockException;

import org.apache.dubbo.rpc.Invocation;
import org.apache.dubbo.rpc.Invoker;
import org.apache.dubbo.rpc.Result;

/**
 * Fallback handler for Dubbo services.
 *
 * @author Eric Zhao
 */
@FunctionalInterface
public interface DubboFallback {

    /**
     * Handle the block exception and provide fallback result.
     *
     * @param invoker Dubbo invoker
     * @param invocation Dubbo invocation
     * @param ex block exception
     * @return fallback result
     */
    Result handle(Invoker<?> invoker, Invocation invocation, BlockException ex);
}
