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

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.adapter.dubbo.DubboAdapterGlobalConfig;
import com.alibaba.csp.sentinel.fallback.FallbackRule;
import com.alibaba.csp.sentinel.fallback.FallbackRuleManager;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.SentinelRpcException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.dubbo.common.json.JSON;
import com.alibaba.dubbo.common.json.ParseException;
import com.alibaba.dubbo.rpc.Invocation;
import com.alibaba.dubbo.rpc.Invoker;
import com.alibaba.dubbo.rpc.Result;
import com.alibaba.dubbo.rpc.RpcResult;
import org.junit.Assert;
import org.junit.Test;

/**
 * @author Eric Zhao
 */
public class DubboFallbackRegistryTest {

    @Test
    public void testDefaultFallback() {
        // Test for default fallback.
        BlockException ex = new FlowException("xxx");
        Result result = new DefaultDubboFallback().handle(null, null, ex);
        Assert.assertTrue(result.hasException());
        Assert.assertEquals(SentinelRpcException.class, result.getException().getClass());
    }

    @Test
    public void testCustomFallback() {
        BlockException ex = new FlowException("xxx");
        DubboAdapterGlobalConfig.setConsumerFallback(new DubboFallback() {
            @Override
            public Result handle(Invoker<?> invoker, Invocation invocation, BlockException e) {
                return new RpcResult("Error: " + e.getClass().getName());
            }

            @Override
            public Result handle(Invoker<?> invoker, Invocation invocation, BlockException ex, String resourceName) {
                FallbackRule fallbackRule = FallbackRuleManager.getFallbackRule(resourceName);
                if (StringUtil.isNotEmpty(fallbackRule.getFallback())) {
                    // return null
                    if (Constants.NULL_FALLBACK.equals(fallbackRule.getFallback())) {
                        return null;
                        // throw exception
                    } else if (Constants.EXCEPTION_FALLBACK.equals(fallbackRule.getFallback())) {
                        throw new SentinelRpcException(ex.toRuntimeException());
                        // return fallback object
                    } else {
                        if (StringUtil.isNotEmpty(fallbackRule.getClazzReference())) {
                            RpcResult result = new RpcResult();
                            try {
                                result.setValue(JSON.parse(fallbackRule.getFallback(), fallbackRule.getClass()));
                            } catch (ParseException e) {
                                result.setException(new SentinelRpcException(e.getMessage()));
                            }
                            return result;
                        } else {
                            return new RpcResult("Error: " + ex.getClass().getName());
                        }
                    }
                } else {
                    return new RpcResult("Error: " + ex.getClass().getName());
                }
            }
        });
        Result result = DubboAdapterGlobalConfig.getConsumerFallback()
                .handle(null, null, ex);
        Assert.assertFalse("The invocation should not fail", result.hasException());
        Assert.assertEquals("Error: " + ex.getClass().getName(), result.getValue());
    }
}
