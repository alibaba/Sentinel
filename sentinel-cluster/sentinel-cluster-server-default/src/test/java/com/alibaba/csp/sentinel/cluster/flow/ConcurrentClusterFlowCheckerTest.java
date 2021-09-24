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
package com.alibaba.csp.sentinel.cluster.flow;

import com.alibaba.csp.sentinel.cluster.TokenResult;
import com.alibaba.csp.sentinel.cluster.TokenResultStatus;
import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.flow.statistic.concurrent.CurrentConcurrencyManager;
import com.alibaba.csp.sentinel.cluster.flow.statistic.concurrent.TokenCacheNodeManager;
import com.alibaba.csp.sentinel.cluster.server.connection.ConnectionManager;
import com.alibaba.csp.sentinel.slots.block.ClusterRuleConstant;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.ClusterFlowConfig;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.test.AbstractTimeBasedTest;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author yunfeiyanggzq
 */
public class ConcurrentClusterFlowCheckerTest extends AbstractTimeBasedTest {
    @Before
    public void setUp() {
        FlowRule rule = new FlowRule();
        ClusterFlowConfig config = new ClusterFlowConfig();
        config.setResourceTimeout(500);
        config.setClientOfflineTime(1000);
        config.setFlowId(111L);
        config.setThresholdType(ClusterRuleConstant.FLOW_THRESHOLD_GLOBAL);
        rule.setClusterConfig(config);
        rule.setClusterMode(true);
        rule.setCount(10);
        rule.setResource("test");
        rule.setGrade(RuleConstant.FLOW_GRADE_THREAD);
        ArrayList<FlowRule> rules = new ArrayList<>();
        rules.add(rule);
        ClusterFlowRuleManager.registerPropertyIfAbsent("1-name");
        ClusterFlowRuleManager.loadRules("1-name", rules);
    }

    @Test
    public void testEasyAcquireAndRelease() throws InterruptedException {
        setCurrentMillis(System.currentTimeMillis());
        FlowRule rule = ClusterFlowRuleManager.getFlowRuleById(111L);
        ArrayList<Long> list = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            TokenResult result = ConcurrentClusterFlowChecker.acquireConcurrentToken("127.0.0.1", rule, 1);
            Assert.assertTrue("fail to acquire token",
                    result.getStatus() == TokenResultStatus.OK && result.getTokenId() != 0);
            list.add(result.getTokenId());
        }
        for (int i = 0; i < 10; i++) {
            TokenResult result = ConcurrentClusterFlowChecker.acquireConcurrentToken("127.0.0.1", rule, 1);
            Assert.assertTrue("fail to acquire block token",
                    result.getStatus() == TokenResultStatus.BLOCKED);
        }
        for (int i = 0; i < 10; i++) {
            TokenResult result = ConcurrentClusterFlowChecker.releaseConcurrentToken(list.get(i));
            Assert.assertTrue("fail to release token",
                    result.getStatus() == TokenResultStatus.RELEASE_OK);
        }
        Assert.assertTrue("fail to release token",
                CurrentConcurrencyManager.get(111L).get() == 0 && TokenCacheNodeManager.getSize() == 0);
    }

    @Test
    public void testConcurrentAcquireAndRelease() throws InterruptedException {
        setCurrentMillis(System.currentTimeMillis());
        final FlowRule rule = ClusterFlowRuleManager.getFlowRuleById(111L);
        final CountDownLatch countDownLatch = new CountDownLatch(1000);
        ExecutorService pool = Executors.newFixedThreadPool(100);

        for (long i = 0; i < 1000; i++) {
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    assert rule != null;
                    TokenResult result = ConcurrentClusterFlowChecker.acquireConcurrentToken("127.0.0.1", rule, 1);
                    Assert.assertTrue("concurrent control fail", CurrentConcurrencyManager.get(111L).get() <= rule.getCount());
                    if (result.getStatus() == TokenResultStatus.OK) {
                        ConcurrentClusterFlowChecker.releaseConcurrentToken(result.getTokenId());
                    }
                    countDownLatch.countDown();
                }
            };
            pool.execute(task);
        }
        countDownLatch.await();
        pool.shutdown();
        assert rule != null;
        Assert.assertTrue("fail to acquire and release token",
                CurrentConcurrencyManager.get(rule.getClusterConfig().getFlowId()).get() == 0 && TokenCacheNodeManager.getSize() == 0);
    }

    @Test
    public void testReleaseExpiredToken() throws InterruptedException {
        ConnectionManager.addConnection("test", "127.0.0.1");
        FlowRule rule = ClusterFlowRuleManager.getFlowRuleById(111L);
        for (int i = 0; i < 10; i++) {
            ConcurrentClusterFlowChecker.acquireConcurrentToken("127.0.0.1", rule, 1);
        }
        Thread.sleep(3000);
        Assert.assertTrue("fail to acquire and release token", CurrentConcurrencyManager.get(rule.getClusterConfig().getFlowId()).get() == 0 && TokenCacheNodeManager.getSize() == 0);
    }
}
