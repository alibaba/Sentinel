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
package com.alibaba.csp.sentinel.slots.block.flow;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.cluster.ClusterStateManager;
import com.alibaba.csp.sentinel.cluster.TokenResult;
import com.alibaba.csp.sentinel.cluster.TokenResultStatus;
import com.alibaba.csp.sentinel.cluster.TokenService;
import com.alibaba.csp.sentinel.cluster.client.TokenClientProvider;
import com.alibaba.csp.sentinel.cluster.server.EmbeddedClusterTokenServerProvider;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.slotchain.ResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.timeout.ReSourceTimeoutStrategy;
import com.alibaba.csp.sentinel.slots.block.flow.timeout.ReSourceTimeoutStrategyUtil;
import com.alibaba.csp.sentinel.slots.block.flow.timeout.TimerHolder;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;
import com.alibaba.csp.sentinel.util.HostNameUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.csp.sentinel.util.function.Function;
import io.netty.util.Timeout;
import io.netty.util.TimerTask;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Rule checker for flow control rules.
 *
 * @author Eric Zhao
 */
public class FlowRuleChecker {

    public void checkFlow(Function<String, Collection<FlowRule>> ruleProvider, ResourceWrapper resource,
                          Context context, DefaultNode node, int count, boolean prioritized) throws BlockException {
        if (ruleProvider == null || resource == null) {
            return;
        }
        Collection<FlowRule> rules = ruleProvider.apply(resource.getName());
        if (rules != null) {
            for (FlowRule rule : rules) {
                if (!canPassCheck(rule, context, node, count, prioritized)) {
                    throw new FlowException(rule.getLimitApp(), rule);
                }
            }
        }
    }

    public void release(Context context) {
//        System.out.println("开始释放");
        releaseFlowToken(context);
//        System.out.println("释放结束");
    }

    private static void releaseFlowToken(Context context) {
        Entry entry = context.getCurEntry();
        if (entry == null) {
            return;
        }
        long tokenId = entry.getTokenId();
        if (tokenId == 0) {
            return;
        }
        Timeout timeout = ReSourceTimeoutStrategyUtil.getTimeout(tokenId);
        if (timeout == null) {
            releaseToken(tokenId);
            return;
        }
        if (timeout.isCancelled() || timeout.isExpired()) {
            ReSourceTimeoutStrategyUtil.clearByTokenId(tokenId);
            return;
        }
        releaseToken(tokenId);
        timeout.cancel();
        ReSourceTimeoutStrategyUtil.clearByTokenId(tokenId);
    }


    public boolean canPassCheck(/*@NonNull*/ FlowRule rule, Context context, DefaultNode node,
                                             int acquireCount) {
        return canPassCheck(rule, context, node, acquireCount, false);
    }

    public boolean canPassCheck(/*@NonNull*/ FlowRule rule, Context context, DefaultNode node, int acquireCount,
                                             boolean prioritized) {
        String limitApp = rule.getLimitApp();
        if (limitApp == null) {
            return true;
        }

        if (rule.isClusterMode()) {
            return passClusterCheck(rule, context, node, acquireCount, prioritized);
        }

        return passLocalCheck(rule, context, node, acquireCount, prioritized);
    }

    private static boolean passLocalCheck(FlowRule rule, Context context, DefaultNode node, int acquireCount,
                                          boolean prioritized) {
        Node selectedNode = selectNodeByRequesterAndStrategy(rule, context, node);
        if (selectedNode == null) {
            return true;
        }

        return rule.getRater().canPass(selectedNode, acquireCount, prioritized);
    }

    private static void releaseToken(long tokenId) {
        TokenService clusterService = pickClusterService();
        if (clusterService == null) {
            return;
        }

        TokenResult result = clusterService.releaseConcurrentToken(tokenId);
        if (result.getStatus() != TokenResultStatus.RELEASE_OK) {
            RecordLog.warn("[FlowRuleChecker] release cluster token unexpected failed", result.getStatus());
            return;
        }

    }

    static Node selectReferenceNode(FlowRule rule, Context context, DefaultNode node) {
        String refResource = rule.getRefResource();
        int strategy = rule.getStrategy();

        if (StringUtil.isEmpty(refResource)) {
            return null;
        }

        if (strategy == RuleConstant.STRATEGY_RELATE) {
            return ClusterBuilderSlot.getClusterNode(refResource);
        }

        if (strategy == RuleConstant.STRATEGY_CHAIN) {
            if (!refResource.equals(context.getName())) {
                return null;
            }
            return node;
        }
        // No node.
        return null;
    }

    private static boolean filterOrigin(String origin) {
        // Origin cannot be `default` or `other`.
        return !RuleConstant.LIMIT_APP_DEFAULT.equals(origin) && !RuleConstant.LIMIT_APP_OTHER.equals(origin);
    }

    static Node selectNodeByRequesterAndStrategy(/*@NonNull*/ FlowRule rule, Context context, DefaultNode node) {
        // The limit app should not be empty.
        String limitApp = rule.getLimitApp();
        int strategy = rule.getStrategy();
        String origin = context.getOrigin();

        if (limitApp.equals(origin) && filterOrigin(origin)) {
            if (strategy == RuleConstant.STRATEGY_DIRECT) {
                // Matches limit origin, return origin statistic node.
                return context.getOriginNode();
            }

            return selectReferenceNode(rule, context, node);
        } else if (RuleConstant.LIMIT_APP_DEFAULT.equals(limitApp)) {
            if (strategy == RuleConstant.STRATEGY_DIRECT) {
                // Return the cluster node.
                return node.getClusterNode();
            }

            return selectReferenceNode(rule, context, node);
        } else if (RuleConstant.LIMIT_APP_OTHER.equals(limitApp)
                && FlowRuleManager.isOtherOrigin(origin, rule.getResource())) {
            if (strategy == RuleConstant.STRATEGY_DIRECT) {
                return context.getOriginNode();
            }

            return selectReferenceNode(rule, context, node);
        }

        return null;
    }

    public static boolean passClusterCheck(FlowRule rule, Context context, DefaultNode node, int acquireCount,
                                           boolean prioritized) {
        try {
            TokenService clusterService = pickClusterService();
            if (clusterService == null) {
                return fallbackToLocalOrPass(rule, context, node, acquireCount, prioritized);
            }
            TokenResult result = requestToken(clusterService, rule, context, node, acquireCount, prioritized);
            return applyTokenResult(result, rule, context, node, acquireCount, prioritized);
            // If client is absent, then fallback to local mode.
        } catch (Throwable ex) {
            RecordLog.warn("[FlowRuleChecker] Request cluster token unexpected failed", ex);
        }
        // Fallback to local flow control when token client or server for this rule is not available.
        // If fallback is not enabled, then directly pass.
        return fallbackToLocalOrPass(rule, context, node, acquireCount, prioritized);
    }

    private static TokenResult requestToken(TokenService clusterService, FlowRule rule, Context context, DefaultNode node, int acquireCount, boolean prioritized) {
        int grade = rule.getGrade();
        long flowId = rule.getClusterConfig().getFlowId();
        if (grade == RuleConstant.FLOW_GRADE_THREAD) {
            String address = null;
            if (ClusterStateManager.isServer()) {
                address = HostNameUtil.getIp();
            }
            return clusterService.requestConcurrentToken(address, flowId, acquireCount);
        } else if (grade == RuleConstant.FLOW_GRADE_QPS) {
            return clusterService.requestToken(flowId, acquireCount, prioritized);
        } else {
            RecordLog.warn("[FlowRuleChecker] Request cluster token unexpected grade,just pass", grade);
            return new TokenResult(TokenResultStatus.OK);
        }
    }

    private static boolean fallbackToLocalOrPass(FlowRule rule, Context context, DefaultNode node, int acquireCount,
                                                 boolean prioritized) {
        if (rule.getClusterConfig().isFallbackToLocalWhenFail()) {
            return passLocalCheck(rule, context, node, acquireCount, prioritized);
        } else {
            // The rule won't be activated, just pass.
            return true;
        }
    }

    public static TokenService pickClusterService() {
        if (ClusterStateManager.isClient()) {
            return TokenClientProvider.getClient();
        }
        if (ClusterStateManager.isServer()) {
            return EmbeddedClusterTokenServerProvider.getServer();
        }
        return null;
    }

    private static boolean applyTokenResult(/*@NonNull*/ TokenResult result, FlowRule rule, Context context,
                                                         DefaultNode node,
                                                         int acquireCount, boolean prioritized) {
        switch (result.getStatus()) {
            case TokenResultStatus.OK:
                // Store the tokenId and start a resource timeout timer if the resource timeout strategy isn't default.
                if (rule.getGrade() == RuleConstant.FLOW_GRADE_THREAD) {
                    setEntry(rule, context, result.getTokenId());
                }
                return true;
            case TokenResultStatus.SHOULD_WAIT:
                // Wait for next tick.
                try {
                    Thread.sleep(result.getWaitInMs());
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return true;
            case TokenResultStatus.NO_RULE_EXISTS:
            case TokenResultStatus.BAD_REQUEST:
            case TokenResultStatus.FAIL:
            case TokenResultStatus.TOO_MANY_REQUEST:
                return fallbackToLocalOrPass(rule, context, node, acquireCount, prioritized);
            case TokenResultStatus.BLOCKED:
                if (rule.getGrade() == RuleConstant.FLOW_GRADE_THREAD) {
                    return ConcurrentFlowBlockStrategy.canPass(rule, context, node, acquireCount, prioritized);
                }
            default:
                return false;
        }
    }

    private static void setEntry(final FlowRule rule, final Context context, final long tokenId) {
        final Entry entry = context.getCurEntry();
        Timeout timeout = null;
        if (rule.getClusterConfig().getResourceTimeoutStrategy() != RuleConstant.DEFAULT_RESOURCE_TIMEOUT_STRATEGY) {
            timeout = TimerHolder.getTimer().newTimeout(new TimerTask() {
                @Override
                public void run(Timeout timeout) {
                    ReSourceTimeoutStrategy strategy = ReSourceTimeoutStrategyUtil.getTimeoutStrategy(rule.getClusterConfig().getResourceTimeoutStrategy());
                    strategy.doWithSourceTimeout(tokenId);
                }
            }, rule.getClusterConfig().getResourceTimeout(), TimeUnit.MILLISECONDS);
        }
        if (entry.getTokenId() != 0) {
            releaseFlowToken(context);
        }
        entry.setTokenId(tokenId);
        ReSourceTimeoutStrategyUtil.addTimeout(tokenId, timeout);
    }
}
