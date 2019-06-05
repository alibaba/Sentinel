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

import com.alibaba.csp.sentinel.slots.block.flow.FlowException;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author tiger
 */
public class ZuulBlockFallbackProviderTest {

    private String ALL_ROUTE = "*";

    @Test
    public void testGetNullRoute() throws Exception {
        ZuulBlockFallbackProvider fallbackProvider = ZuulBlockFallbackManager.getFallbackProvider(null);
        Assert.assertEquals(fallbackProvider.getRoute(), ALL_ROUTE);
    }

    @Test
    public void testGetDefaultRoute() throws Exception {
        ZuulBlockFallbackProvider fallbackProvider = ZuulBlockFallbackManager.getFallbackProvider(ALL_ROUTE);
        Assert.assertEquals(fallbackProvider.getRoute(), ALL_ROUTE);
    }

    @Test
    public void testGetNotInCacheRoute() throws Exception {
        ZuulBlockFallbackProvider fallbackProvider = ZuulBlockFallbackManager.getFallbackProvider("/not/in");
        Assert.assertEquals(fallbackProvider.getRoute(), ALL_ROUTE);
    }

    @Test
    public void testFlowControlFallbackResponse() throws Exception {
        ZuulBlockFallbackProvider fallbackProvider = ZuulBlockFallbackManager.getFallbackProvider(ALL_ROUTE);
        BlockResponse clientHttpResponse = fallbackProvider.fallbackResponse(ALL_ROUTE,
            new FlowException("flow exception"));
        Assert.assertEquals(clientHttpResponse.getCode(), 429);
    }

    @Test
    public void testRuntimeExceptionFallbackResponse() throws Exception {
        ZuulBlockFallbackProvider fallbackProvider = ZuulBlockFallbackManager.getFallbackProvider(ALL_ROUTE);
        BlockResponse clientHttpResponse = fallbackProvider.fallbackResponse(ALL_ROUTE, new RuntimeException());
        Assert.assertEquals(clientHttpResponse.getCode(), 500);
    }
}