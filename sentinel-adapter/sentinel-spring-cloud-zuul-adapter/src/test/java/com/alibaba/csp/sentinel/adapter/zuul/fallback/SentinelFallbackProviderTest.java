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

package com.alibaba.csp.sentinel.adapter.zuul.fallback;

import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpResponse;

/**
 * @author tiger
 */
public class SentinelFallbackProviderTest {

    private String ALL_ROUTE = "*";

    @Test
    public void testGetNullRoute() throws Exception {
        SentinelFallbackProvider fallbackProvider = SentinelFallbackManager.getFallbackProvider(null);
        Assert.assertEquals(fallbackProvider.getRoute(), ALL_ROUTE);
    }

    @Test
    public void testGetDefaultRoute() throws Exception {
        SentinelFallbackProvider fallbackProvider = SentinelFallbackManager.getFallbackProvider(ALL_ROUTE);
        Assert.assertEquals(fallbackProvider.getRoute(), ALL_ROUTE);
    }

    @Test
    public void testGetNotInCacheRoute() throws Exception {
        SentinelFallbackProvider fallbackProvider = SentinelFallbackManager.getFallbackProvider("/not/in");
        Assert.assertEquals(fallbackProvider.getRoute(), ALL_ROUTE);
    }

    @Test
    public void testFlowControlFallbackResponse() throws Exception {
        SentinelFallbackProvider fallbackProvider = SentinelFallbackManager.getFallbackProvider(ALL_ROUTE);
        ClientHttpResponse clientHttpResponse = fallbackProvider.fallbackResponse(ALL_ROUTE, new FlowException("flow exception"));
        Assert.assertEquals(clientHttpResponse.getRawStatusCode(), HttpStatus.TOO_MANY_REQUESTS.value());
    }

    @Test
    public void testRuntimeExceptionFallbackResponse() throws Exception {
        SentinelFallbackProvider fallbackProvider = SentinelFallbackManager.getFallbackProvider(ALL_ROUTE);
        ClientHttpResponse clientHttpResponse = fallbackProvider.fallbackResponse(ALL_ROUTE, new RuntimeException());
        Assert.assertEquals(clientHttpResponse.getRawStatusCode(), HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}