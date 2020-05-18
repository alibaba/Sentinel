/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.dashboard.service.impl.rule.checker;

import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.flow.AddFlowRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.flow.DeleteFlowRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.flow.UpdateFlowRuleReqVo;

import static com.alibaba.csp.sentinel.slots.block.RuleConstant.*;
import static com.alibaba.csp.sentinel.slots.block.ClusterRuleConstant.*;
import static com.alibaba.csp.sentinel.dashboard.service.impl.common.ParamChecker.*;

/**
 * @author cdfive
 */
public class FlowRuleVoChecker {

    public static void checkAdd(AddFlowRuleReqVo reqVo) {
        checkNotNull(reqVo, "body");

        checkNotBlank(reqVo.getApp(), "app");

        checkNotBlank(reqVo.getResource(), "resource");

        checkNotBlank(reqVo.getLimitApp(), "limitApp");

        checkNotNull(reqVo.getGrade(), "grade");
        checkInValues(reqVo.getGrade(), "grade", FLOW_GRADE_THREAD, FLOW_GRADE_QPS);

        checkNotNull(reqVo.getCount(), "count");
        checkCondition(reqVo.getCount() >= 0, "count must be greater than or equal to 0");

        checkNotNull(reqVo.getStrategy(), "strategy");
        checkInValues(reqVo.getStrategy(), "strategy", STRATEGY_DIRECT, STRATEGY_RELATE, STRATEGY_CHAIN);
        if (reqVo.getStrategy() != STRATEGY_DIRECT) {
            checkNotBlankMessage(reqVo.getRefResource(), "refResource can't be null or empty when strategy is " + STRATEGY_RELATE + " or " + STRATEGY_CHAIN);
        }

        checkNotNull(reqVo.getControlBehavior(), "controlBehavior");
        checkInValues(reqVo.getControlBehavior(), "controlBehavior", CONTROL_BEHAVIOR_DEFAULT, CONTROL_BEHAVIOR_WARM_UP, CONTROL_BEHAVIOR_RATE_LIMITER);
        if (reqVo.getControlBehavior() == CONTROL_BEHAVIOR_WARM_UP) {
            checkNotNullMessage(reqVo.getWarmUpPeriodSec(), "warmUpPeriodSec can't be null when controlBehavior is " + CONTROL_BEHAVIOR_WARM_UP);
            checkCondition(reqVo.getWarmUpPeriodSec() > 0, "warmUpPeriodSec must be greater than 0 when controlBehavior is " + CONTROL_BEHAVIOR_WARM_UP);
        }
        if (reqVo.getControlBehavior() == CONTROL_BEHAVIOR_RATE_LIMITER) {
            checkNotNullMessage(reqVo.getMaxQueueingTimeMs(), "maxQueueingTimeMs can't be null when controlBehavior is " + CONTROL_BEHAVIOR_RATE_LIMITER);
            checkCondition(reqVo.getMaxQueueingTimeMs() > 0, "maxQueueingTimeMs must be greater than 0 when controlBehavior is " + CONTROL_BEHAVIOR_RATE_LIMITER);
        }

        checkNotNull(reqVo.getClusterMode(), "clusterMode");
        if (reqVo.getClusterMode()) {
            AddFlowRuleReqVo.ClusterConfigReqVo clusterConfigReqVo = reqVo.getClusterConfig();
            checkNotNull(clusterConfigReqVo, "clusterConfig");

            checkNotNull(clusterConfigReqVo.getThresholdType(), "thresholdType");
            checkInValues(clusterConfigReqVo.getThresholdType(), "thresholdType", FLOW_THRESHOLD_AVG_LOCAL, FLOW_THRESHOLD_GLOBAL);

            checkNotNull(clusterConfigReqVo.getFallbackToLocalWhenFail(), "fallbackToLocalWhenFail");
        }
    }

    public static void checkUpdate(UpdateFlowRuleReqVo reqVo) {
        checkNotNull(reqVo, "body");

        checkNotBlank(reqVo.getApp(), "app");

        checkNotNull(reqVo.getId(), "id");
        checkCondition(reqVo.getId() > 0, "id must be greater than 0");

        checkNotBlank(reqVo.getLimitApp(), "limitApp");

        checkNotNull(reqVo.getGrade(), "grade");
        checkInValues(reqVo.getGrade(), "grade", FLOW_GRADE_THREAD, FLOW_GRADE_QPS);

        checkNotNull(reqVo.getCount(), "count");
        checkCondition(reqVo.getCount() >= 0, "count must be greater than or equal to 0");

        checkNotNull(reqVo.getStrategy(), "strategy");
        checkInValues(reqVo.getStrategy(), "strategy", STRATEGY_DIRECT, STRATEGY_RELATE, STRATEGY_CHAIN);
        if (reqVo.getStrategy() != STRATEGY_DIRECT) {
            checkNotBlankMessage(reqVo.getRefResource(), "refResource can't be null or empty when strategy is " + STRATEGY_RELATE + " or " + STRATEGY_CHAIN);
        }

        checkNotNull(reqVo.getControlBehavior(), "controlBehavior");
        checkInValues(reqVo.getControlBehavior(), "controlBehavior", CONTROL_BEHAVIOR_DEFAULT, CONTROL_BEHAVIOR_WARM_UP, CONTROL_BEHAVIOR_RATE_LIMITER);
        if (reqVo.getControlBehavior() == CONTROL_BEHAVIOR_WARM_UP) {
            checkNotNullMessage(reqVo.getWarmUpPeriodSec(), "warmUpPeriodSec can't be null when controlBehavior is " + CONTROL_BEHAVIOR_WARM_UP);
            checkCondition(reqVo.getWarmUpPeriodSec() > 0, "warmUpPeriodSec must be greater than 0 when controlBehavior is " +CONTROL_BEHAVIOR_WARM_UP);
        }
        if (reqVo.getControlBehavior() == CONTROL_BEHAVIOR_RATE_LIMITER) {
            checkNotNullMessage(reqVo.getMaxQueueingTimeMs(), "maxQueueingTimeMs can't be null when controlBehavior is " + CONTROL_BEHAVIOR_RATE_LIMITER);
            checkCondition(reqVo.getMaxQueueingTimeMs() > 0, "maxQueueingTimeMs must be greater than 0 when controlBehavior is " + CONTROL_BEHAVIOR_RATE_LIMITER);
        }

        checkNotNull(reqVo.getClusterMode(), "clusterMode");
        if (reqVo.getClusterMode()) {
            AddFlowRuleReqVo.ClusterConfigReqVo clusterConfigReqVo = reqVo.getClusterConfig();
            checkNotNull(clusterConfigReqVo, "clusterConfig");

            checkNotNull(clusterConfigReqVo.getThresholdType(), "thresholdType");
            checkInValues(clusterConfigReqVo.getThresholdType(), "thresholdType", FLOW_THRESHOLD_AVG_LOCAL, FLOW_THRESHOLD_GLOBAL);

            checkNotNull(clusterConfigReqVo.getFallbackToLocalWhenFail(), "fallbackToLocalWhenFail");
        }
    }

    public static void checkDelete(DeleteFlowRuleReqVo reqVo) {
        checkNotNull(reqVo, "body");

        checkNotBlank(reqVo.getApp(), "app");

        checkNotNull(reqVo.getId(), "id");
        checkCondition(reqVo.getId() > 0, "id must be greater than 0");
    }
}
