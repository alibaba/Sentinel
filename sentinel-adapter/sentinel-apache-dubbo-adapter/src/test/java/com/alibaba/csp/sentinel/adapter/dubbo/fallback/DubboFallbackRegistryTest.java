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

import com.alibaba.csp.sentinel.adapter.dubbo.config.DubboAdapterGlobalConfig;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;

import org.apache.dubbo.rpc.AsyncRpcResult;
import org.apache.dubbo.rpc.Result;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Eric Zhao
 */
public class DubboFallbackRegistryTest {

    @Before
    public void setUp() {
        DubboAdapterGlobalConfig.setConsumerFallback(new DefaultDubboFallback());
    }

    @After
    public void tearDown() {
        DubboAdapterGlobalConfig.setConsumerFallback(new DefaultDubboFallback());
    }

    @Test
    public void testDefaultFallback() {
        // Test for default fallback.
        BlockException ex = new FlowException("xxx");
        Result result = new DefaultDubboFallback().handle(null, null, ex);
        Assert.assertTrue("The result should carry exception", result.hasException());
        Assert.assertTrue(BlockException.isBlockException(result.getException()));
        Assert.assertTrue(result.getException().getMessage().contains(ex.getClass().getSimpleName()));
    }

    @Test
    public void testCustomFallback() {
        BlockException ex = new FlowException("xxx");
        DubboAdapterGlobalConfig.setConsumerFallback(
            (invoker, invocation, e) -> AsyncRpcResult
                .newDefaultAsyncResult("Error: " + e.getClass().getName(), invocation));
        Result result = DubboAdapterGlobalConfig.getConsumerFallback()
            .handle(null, null, ex);
        Assert.assertFalse("The invocation should not fail", result.hasException());
        Assert.assertEquals("Error: " + ex.getClass().getName(), result.getValue());
    }
}
