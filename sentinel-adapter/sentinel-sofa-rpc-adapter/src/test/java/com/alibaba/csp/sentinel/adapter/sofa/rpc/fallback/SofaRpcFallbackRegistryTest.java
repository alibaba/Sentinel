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
package com.alibaba.csp.sentinel.adapter.sofa.rpc.fallback;

import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alipay.sofa.rpc.core.request.SofaRequest;
import com.alipay.sofa.rpc.core.response.SofaResponse;
import com.alipay.sofa.rpc.filter.FilterInvoker;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test cases for {@link SofaRpcFallbackRegistry}.
 *
 * @author cdfive
 */
public class SofaRpcFallbackRegistryTest {

    @Test
    public void testDefaultfallback() {
        // Test get default provider fallback
        SofaRpcFallback providerFallback = SofaRpcFallbackRegistry.getProviderFallback();
        assertNotNull(providerFallback);
        assertTrue(providerFallback instanceof DefaultSofaRpcFallback);

        // Test get default consumer fallback
        SofaRpcFallback consumerFallback = SofaRpcFallbackRegistry.getConsumerFallback();
        assertNotNull(consumerFallback);
        assertTrue(consumerFallback instanceof DefaultSofaRpcFallback);
    }

    @Test
    public void testCustomFallback() {
        // Test invoke custom provider fallback
        SofaRpcFallbackRegistry.setProviderFallback(new SofaRpcFallback() {
            @Override
            public SofaResponse handle(FilterInvoker invoker, SofaRequest request, BlockException ex) {
                SofaResponse response = new SofaResponse();
                response.setAppResponse("test provider response");
                return response;
            }
        });
        SofaResponse providerResponse = SofaRpcFallbackRegistry.getProviderFallback().handle(null, null, null);
        assertNotNull(providerResponse);
        assertEquals("test provider response", providerResponse.getAppResponse());

        // Test invoke custom consumer fallback
        SofaRpcFallbackRegistry.setConsumerFallback(new SofaRpcFallback() {
            @Override
            public SofaResponse handle(FilterInvoker invoker, SofaRequest request, BlockException ex) {
                SofaResponse response = new SofaResponse();
                response.setAppResponse("test consumer response");
                return response;
            }
        });
        SofaResponse consumerResponse = SofaRpcFallbackRegistry.getConsumerFallback().handle(null, null, null);
        assertNotNull(consumerResponse);
        assertEquals("test consumer response", consumerResponse.getAppResponse());
    }
}
