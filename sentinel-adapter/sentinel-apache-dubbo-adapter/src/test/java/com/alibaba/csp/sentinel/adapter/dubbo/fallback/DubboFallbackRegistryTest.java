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

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.SentinelRpcException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;

import org.apache.dubbo.rpc.Result;
import org.apache.dubbo.rpc.RpcResult;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Eric Zhao
 */
public class DubboFallbackRegistryTest {

    @Test(expected = SentinelRpcException.class)
    public void testDefaultFallback() {
        // Test for default.
        BlockException ex = new FlowException("xxx");
        DubboFallbackRegistry.getConsumerFallback()
            .handle(null, null, ex);
    }

    @Test
    public void testCustomFallback() {
        BlockException ex = new FlowException("xxx");
        DubboFallbackRegistry.setConsumerFallback(
            (invoker, invocation, e) -> new RpcResult("Error: " + e.getClass().getName()));
        Result result = DubboFallbackRegistry.getConsumerFallback()
            .handle(null, null, ex);
        Assert.assertFalse("The invocation should not fail", result.hasException());
        Assert.assertEquals("Error: " + ex.getClass().getName(), result.getValue());
    }
}
