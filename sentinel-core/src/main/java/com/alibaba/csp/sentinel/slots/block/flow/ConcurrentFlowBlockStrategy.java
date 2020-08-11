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

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author yunfeiyanggzq
 */
final public class ConcurrentFlowBlockStrategy {
    public static boolean canPass(FlowRule rule, Context context, DefaultNode node, int acquireCount, boolean prioritized) {
        if (!prioritized) {
            return false;
        }
        int strategy = rule.getClusterConfig().getAcquireRefuseStrategy();
        switch (strategy) {
            case RuleConstant.TRY_AGAIN_BLOCK_STRATEGY:
                return tryAcquireToken(rule, context, node, acquireCount, true, false);
            case RuleConstant.TRY_UNTIL_SUCCESS_BLOCK_STRATEGY:
                return tryAcquireToken(rule, context, node, acquireCount, true, true);
            default:
                return false;
        }
    }

    private static void sleep(DefaultNode node, FlowRule rule) {
        try {
            int sleepTime = 0;
            ClusterNode clusterNode = node.getClusterNode();
            if (clusterNode.avgRt() != 0 && clusterNode.blockQps() != 0 && clusterNode.passQps() != 0) {
                sleepTime = (int) (clusterNode.avgRt() * clusterNode.blockQps() / rule.getCount());
            } else {
                sleepTime = new Random().nextInt(300);
            }
            sleepTime = sleepTime == 0 ? 10 : sleepTime;
            System.out.println("睡眠"+sleepTime);
            TimeUnit.MILLISECONDS.sleep(sleepTime);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static boolean tryAcquireToken(FlowRule rule, Context context, DefaultNode node, int acquireCount, boolean prioritized, boolean loop) {
        sleep(node, rule);
        return FlowRuleChecker.passClusterCheck(rule, context, node, acquireCount, loop);
    }
}
