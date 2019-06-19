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

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.alibaba.csp.sentinel.cluster.TokenResultStatus;
import com.alibaba.csp.sentinel.cluster.TokenResult;
import com.alibaba.csp.sentinel.cluster.TokenService;
import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterParamFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.server.ServerConstants;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;

/**
 * Default implementation for cluster {@link TokenService}.
 *
 * @author Eric Zhao
 * @since 1.4.0
 */
public class DefaultTokenService implements TokenService {

    @Override
    public TokenResult requestToken(Long ruleId, int acquireCount, boolean prioritized) {
        if (notValidRequest(ruleId, acquireCount)) {
            return badRequest();
        }
        // The rule should be valid.
        FlowRule rule = ClusterFlowRuleManager.getFlowRuleById(ruleId);
        if (rule == null) {
            return new TokenResult(TokenResultStatus.NO_RULE_EXISTS);
        }

        return ClusterFlowChecker.acquireClusterToken(rule, acquireCount, prioritized);
    }

    @Override
    public TokenResult requestParamToken(Long ruleId, int acquireCount, Collection<Object> params) {
        if (notValidRequest(ruleId, acquireCount) || params == null || params.isEmpty()) {
            return badRequest();
        }
        // The rule should be valid.
        ParamFlowRule rule = ClusterParamFlowRuleManager.getParamRuleById(ruleId);
        if (rule == null) {
            return new TokenResult(TokenResultStatus.NO_RULE_EXISTS);
        }

        // Note: here the `params` is the collection of one paramIdx (i.e. the rule retrieved).
        return ClusterParamFlowChecker.acquireClusterToken(rule, acquireCount, params);
    }

    private TokenResult requestParamTokenSingleParam(Long ruleId, int acquireCount, Map<Integer, Object> paramMap) {
        if (notValidRequest(ruleId, acquireCount) || paramMap == null || paramMap.isEmpty()) {
            return badRequest();
        }
        // The rule should be valid.
        ParamFlowRule rule = ClusterParamFlowRuleManager.getParamRuleById(ruleId);
        if (rule == null) {
            return new TokenResult(TokenResultStatus.NO_RULE_EXISTS);
        }
        Object param = paramMap.get(rule.getParamIdx());
        if (param == null) {
            return badRequest();
        }

        return ClusterParamFlowChecker.acquireClusterToken(rule, acquireCount, Collections.singleton(param));
    }

    @Override
    public TokenResult batchRequestToken(Set<Long> ruleIds, int acquireCount, boolean prioritized) {
        if (ruleIds == null || ruleIds.isEmpty()) {
            return badRequest();
        }
        Map<Long, TokenResult> resultMap = new HashMap<>(ruleIds.size());
        for (Long flowId : ruleIds) {
            // TODO: GlobalRequestLimiter should add size or just 1?
            resultMap.put(flowId, requestToken(flowId, acquireCount, prioritized));
        }
        return generateBatchTokenResult(resultMap);
    }

    @Override
    public TokenResult batchRequestParamToken(Set<Long> ruleIds, int acquireCount, Map<Integer, Object> paramMap) {
        if (ruleIds == null || ruleIds.isEmpty() || paramMap == null || paramMap.isEmpty()) {
            return badRequest();
        }
        Map<Long, TokenResult> resultMap = new HashMap<>(ruleIds.size());
        for (Long flowId : ruleIds) {
            // Note: here the `params` is the map of all cluster-scope paramIdx (provided cluster rules).
            // TODO: GlobalRequestLimiter should add size or just 1?
            resultMap.put(flowId, requestParamTokenSingleParam(flowId, acquireCount, paramMap));
        }
        return generateBatchTokenResult(resultMap);
    }

    TokenResult generateBatchTokenResult(Map<Long, TokenResult> resultMap) {
        int waitInMs = 0;
        for (Map.Entry<Long, TokenResult> e : resultMap.entrySet()) {
            TokenResult result = e.getValue();
            switch (result.getStatus()) {
                case TokenResultStatus.OK:
                    continue;
                case TokenResultStatus.SHOULD_WAIT:
                    waitInMs = Math.max(waitInMs, result.getWaitInMs());
                    continue;
                default:
                    Map<String, Object> attachment = new HashMap<>(2);
                    attachment.put(ServerConstants.ATTR_KEY_BLOCK_ID, e.getKey());
                    return new TokenResult(result.getStatus()).setAttachments(attachment);
            }
        }
        int status = waitInMs > 0 ? TokenResultStatus.SHOULD_WAIT : TokenResultStatus.OK;
        return new TokenResult(status).setWaitInMs(waitInMs);
    }

    private boolean notValidRequest(Long id, int count) {
        return id == null || id <= 0 || count <= 0;
    }

    private TokenResult badRequest() {
        return new TokenResult(TokenResultStatus.BAD_REQUEST);
    }
}
