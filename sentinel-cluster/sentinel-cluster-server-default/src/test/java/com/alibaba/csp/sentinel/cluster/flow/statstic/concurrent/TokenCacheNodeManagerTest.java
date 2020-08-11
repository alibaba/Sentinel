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
package com.alibaba.csp.sentinel.cluster.flow.statstic.concurrent;

import com.alibaba.csp.sentinel.cluster.flow.statistic.concurrent.TokenCacheNode;
import com.alibaba.csp.sentinel.cluster.flow.statistic.concurrent.TokenCacheNodeManager;
import com.alibaba.csp.sentinel.test.AbstractTimeBasedTest;
import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class TokenCacheNodeManagerTest extends AbstractTimeBasedTest {
    @Test
    public void testPutTokenCacheNode() throws InterruptedException {
        setCurrentMillis(System.currentTimeMillis());

        for (long i = 0; i < 100; i++) {
            final TokenCacheNode node = new TokenCacheNode();
            node.setTokenId(i);
            node.setFlowId(111L);
            node.setResourceTimeout(10000L);
            node.setClientTimeout(10000L);
            node.setClientAddress("localhost");
            if (TokenCacheNodeManager.validToken(node)) {
                TokenCacheNodeManager.putTokenCacheNode(node.getTokenId(), node);

            }
        }
        Assert.assertEquals(100, TokenCacheNodeManager.getSize());
        for (int i = 0; i < 100; i++) {
            TokenCacheNodeManager.getTokenCacheNode((long) (Math.random() * 100));
        }
        List<Long> keyList = new ArrayList<>(TokenCacheNodeManager.getCache().keySet());
        for (int i = 0; i < 100; i++) {
            Assert.assertEquals(i, (long) keyList.get(i));
        }
    }
}
