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
import org.springframework.http.client.ClientHttpResponse;


/**
 * @author tiger
 */
public class SentinelFallbackManagerTest {

    private String ROUTE = "/test";

    private String DEFAULT_ROUTE = "*";

    class MyNullResponseFallBackProvider implements SentinelFallbackProvider {
        @Override
        public String getRoute() {
            return ROUTE;
        }

        @Override
        public ClientHttpResponse fallbackResponse(String route, Throwable cause) {
            return null;
        }
    }

    @Test
    public void testRegisterProvider() throws Exception {
        MyNullResponseFallBackProvider myNullResponseFallBackProvider = new MyNullResponseFallBackProvider();
        SentinelFallbackManager.registerProvider(myNullResponseFallBackProvider);
        Assert.assertEquals(myNullResponseFallBackProvider.getRoute(), ROUTE);
        Assert.assertNull(myNullResponseFallBackProvider.fallbackResponse(ROUTE, new FlowException("flow ex")));
    }

    @Test
    public void clear() {
        MyNullResponseFallBackProvider myNullResponseFallBackProvider = new MyNullResponseFallBackProvider();
        SentinelFallbackManager.registerProvider(myNullResponseFallBackProvider);
        Assert.assertEquals(myNullResponseFallBackProvider.getRoute(), ROUTE);
        SentinelFallbackManager.clear();
        Assert.assertEquals(SentinelFallbackManager.getFallbackProvider(ROUTE).getRoute(),DEFAULT_ROUTE);
    }

}