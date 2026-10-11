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
package com.alibaba.csp.sentinel.cluster.flow.statistic.concurrent.expire;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import com.alibaba.csp.sentinel.cluster.TokenResult;
import com.alibaba.csp.sentinel.cluster.TokenResultStatus;
import com.alibaba.csp.sentinel.cluster.flow.ConcurrentClusterFlowChecker;
import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.flow.statistic.concurrent.CurrentConcurrencyManager;
import com.alibaba.csp.sentinel.cluster.flow.statistic.concurrent.TokenCacheNode;
import com.alibaba.csp.sentinel.cluster.flow.statistic.concurrent.TokenCacheNodeManager;
import com.alibaba.csp.sentinel.slots.block.ClusterRuleConstant;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.ClusterFlowConfig;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.googlecode.concurrentlinkedhashmap.ConcurrentLinkedHashMap;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;

/**
 * Regression tests for the expired-token cleanup path.
 *
 * <p>{@code RegularExpireStrategy#clearToken} used to call
 * {@code ClusterFlowRuleManager.getFlowRuleById(flowId).getClusterConfig()} without a null
 * check. After a rule is removed by a hot update, {@code getFlowRuleById} returns null and
 * the cleanup task throws an NPE. Worse, the for-loop always scans from the head of the
 * key list, the aborted element is never removed, so every following round aborts on the
 * same node — all expired tokens behind it starve and their concurrency counters leak.
 *
 * <p>Same-origin race in {@code ConcurrentClusterFlowChecker#releaseConcurrentToken}: the
 * concurrency counter can already be removed by a concurrent rule refresh, so
 * {@code CurrentConcurrencyManager.get(flowId)} may return null.
 */
public class RegularExpireStrategyNpeTest {

    private static final String NAMESPACE = "test-regular-expire-namespace";

    @After
    public void tearDown() {
        ClusterFlowRuleManager.loadRules(NAMESPACE, new ArrayList<FlowRule>());
        CurrentConcurrencyManager.remove(888L);
        CurrentConcurrencyManager.remove(999L);
    }

    @Test
    public void clearTokenShouldNotThrowWhenRuleHasBeenDeleted() throws Exception {
        // The flowId is never registered, which simulates a rule removed by a hot update.
        TokenCacheNode node = buildNode(12345L, 1, 10_000L, 10_000L);

        ConcurrentLinkedHashMap<Long, TokenCacheNode> cache = buildCache(node);

        invokeClearToken(cache);

        // The token should be recycled instead of aborting the scan with an NPE.
        Assert.assertNull("token of a deleted rule should be recycled by the cleanup task",
                cache.get(node.getTokenId()));
    }

    @Test
    public void clearTokenShouldContinueScanningBehindNodeWithAbsentRule() throws Exception {
        // Node B's rule is registered normally; its token has already timed out
        // (resourceTimeout lies in the past while resourceTimeoutMs is 1000).
        FlowRule ruleB = buildRule(888L, 10_000L, 1_000L);
        registerRule(ruleB);
        TokenCacheNode nodeA = buildNode(12345L, 1, 10_000L, 10_000L);
        TokenCacheNode nodeB = buildNode(888L, 1, 10_000L, -5_000L);
        // applyClusterFlowRule seeds the counter with 0; set a non-zero value to verify release.
        CurrentConcurrencyManager.put(888L, 5);

        ConcurrentLinkedHashMap<Long, TokenCacheNode> cache = buildCache(nodeA, nodeB);

        invokeClearToken(cache);

        // Without the fix the scan aborts on node A with an NPE and node B starves forever.
        Assert.assertNull("node with absent rule should be recycled", cache.get(nodeA.getTokenId()));
        Assert.assertNull("expired node behind an absent-rule node should also be recycled",
                cache.get(nodeB.getTokenId()));
        Assert.assertEquals("concurrency counter of node B should be released by 1",
                4, CurrentConcurrencyManager.get(888L).get());
    }

    @Test
    public void releaseConcurrentTokenShouldNotThrowWhenCounterHasBeenRemoved() throws Exception {
        FlowRule rule = buildRule(999L, 10_000L, 10_000L);
        registerRule(rule);

        TokenCacheNode node = buildNode(999L, 1, 10_000L, 10_000L);
        TokenCacheNodeManager.putTokenCacheNode(node.getTokenId(), node);

        // Simulate the race window of a concurrent rule refresh, which removes the
        // concurrency counter of flowId 999 before the release path reads it.
        CurrentConcurrencyManager.remove(999L);

        // The release path should not throw an NPE when the counter is absent.
        TokenResult result = ConcurrentClusterFlowChecker.releaseConcurrentToken(node.getTokenId());
        Assert.assertNotNull(result);
        Assert.assertEquals(TokenResultStatus.RELEASE_OK, result.getStatus().intValue());
        Assert.assertNull("token should be removed from the cache",
                TokenCacheNodeManager.getTokenCacheNode(node.getTokenId()));
    }

    /**
     * Builds a token node with the given flowId. {@code clientTimeoutOffsetMs} and
     * {@code resourceTimeoutOffsetMs} are relative to now (negative means in the past).
     */
    private TokenCacheNode buildNode(long flowId, int acquireCount,
                                     long clientTimeoutOffsetMs, long resourceTimeoutOffsetMs) {
        FlowRule rule = buildRule(flowId, clientTimeoutOffsetMs, 1_000L);
        TokenCacheNode node = TokenCacheNode.generateTokenCacheNode(rule, acquireCount, "test-client");
        node.setTokenId(flowId * 1000 + acquireCount);
        node.setClientTimeout(clientTimeoutOffsetMs);
        node.setResourceTimeout(resourceTimeoutOffsetMs);
        return node;
    }

    private FlowRule buildRule(long flowId, long clientOfflineTimeMs, long resourceTimeoutMs) {
        FlowRule rule = new FlowRule("testResource");
        ClusterFlowConfig config = new ClusterFlowConfig();
        config.setFlowId(flowId);
        config.setClientOfflineTime(clientOfflineTimeMs);
        config.setResourceTimeout(resourceTimeoutMs);
        config.setThresholdType(ClusterRuleConstant.FLOW_THRESHOLD_GLOBAL);
        rule.setClusterConfig(config);
        // applyClusterFlowRule silently skips rules that are not in cluster mode.
        rule.setClusterMode(true);
        rule.setGrade(RuleConstant.FLOW_GRADE_THREAD);
        rule.setCount(10);
        return rule;
    }

    private void registerRule(FlowRule rule) {
        List<FlowRule> rules = new ArrayList<FlowRule>();
        rules.add(rule);
        ClusterFlowRuleManager.registerPropertyIfAbsent(NAMESPACE);
        ClusterFlowRuleManager.loadRules(NAMESPACE, rules);
    }

    private ConcurrentLinkedHashMap<Long, TokenCacheNode> buildCache(TokenCacheNode... nodes) {
        ConcurrentLinkedHashMap<Long, TokenCacheNode> cache =
                new ConcurrentLinkedHashMap.Builder<Long, TokenCacheNode>()
                        .maximumWeightedCapacity(1000L)
                        .build();
        for (TokenCacheNode node : nodes) {
            cache.put(node.getTokenId(), node);
        }
        return cache;
    }

    private void invokeClearToken(ConcurrentLinkedHashMap<Long, TokenCacheNode> cache) throws Exception {
        RegularExpireStrategy strategy = new RegularExpireStrategy(cache);
        Method clearToken = RegularExpireStrategy.class.getDeclaredMethod("clearToken");
        clearToken.setAccessible(true);
        try {
            clearToken.invoke(strategy);
        } catch (Exception e) {
            Assert.fail("clearToken should not throw when the rule has been deleted"
                    + " (otherwise the scan aborts on the same node every round and the"
                    + " following expired tokens starve): " + e.getCause());
        }
    }
}
