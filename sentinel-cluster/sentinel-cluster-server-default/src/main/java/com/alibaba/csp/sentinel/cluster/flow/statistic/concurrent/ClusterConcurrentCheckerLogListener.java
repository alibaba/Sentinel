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
package com.alibaba.csp.sentinel.cluster.flow.statistic.concurrent;

import com.alibaba.csp.sentinel.cluster.flow.ConcurrentClusterFlowChecker;
import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterFlowRuleManager;
import com.alibaba.csp.sentinel.cluster.server.log.ClusterServerStatLogUtil;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;

import java.util.Set;

/**
 * @author yunfeiyanggzq
 */
public class ClusterConcurrentCheckerLogListener implements Runnable {
    @Override
    public void run() {
        try {
            collectInformation();
        } catch (Exception e) {
            RecordLog.warn("[ClusterConcurrentCheckerLogListener] Failed to record concurrent flow control  regularly", e);
        }
    }

    private void collectInformation() {
        Set<Long> keySet = CurrentConcurrencyManager.getConcurrencyMapKeySet();
        for (long flowId : keySet) {
            FlowRule rule = ClusterFlowRuleManager.getFlowRuleById(flowId);
            if (rule == null || CurrentConcurrencyManager.get(flowId).get() == 0) {
                continue;
            }
            double concurrencyLevel = ConcurrentClusterFlowChecker.calcGlobalThreshold(rule);
            String resource = rule.getResource();
            ClusterServerStatLogUtil.log(String.format("concurrent|resource:%s|flowId:%dl|concurrencyLevel:%fl|currentConcurrency", resource, flowId,concurrencyLevel),CurrentConcurrencyManager.get(flowId).get());
        }
        if (TokenCacheNodeManager.getSize() != 0){
            ClusterServerStatLogUtil.log("flow|totalTokenSize", TokenCacheNodeManager.getSize());
        }

    }
}
