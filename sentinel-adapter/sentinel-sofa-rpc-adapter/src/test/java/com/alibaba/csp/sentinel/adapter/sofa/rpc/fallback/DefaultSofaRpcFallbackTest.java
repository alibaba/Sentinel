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
import com.alibaba.csp.sentinel.slots.block.SentinelRpcException;
import org.junit.Test;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * Test cases for {@link DefaultSofaRpcFallback}.
 *
 * @author cdfive
 */
public class DefaultSofaRpcFallbackTest {

    @Test
    public void testHandle() {
        SofaRpcFallback sofaRpcFallback = new DefaultSofaRpcFallback();
        BlockException blockException = mock(BlockException.class);

        boolean throwSentinelRpcException = false;
        boolean causeIsBlockException = false;
        try {
            sofaRpcFallback.handle(null, null, blockException);
        } catch (Exception e) {
            throwSentinelRpcException = e instanceof SentinelRpcException;
            causeIsBlockException = e.getCause() instanceof BlockException;
        }
        assertTrue(throwSentinelRpcException);
        assertTrue(causeIsBlockException);
    }
}
