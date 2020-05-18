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

import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.paramflow.AddParamFlowRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.paramflow.DeleteParamFlowRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.paramflow.UpdateParamFlowRuleReqVo;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;

import static com.alibaba.csp.sentinel.dashboard.service.impl.common.ParamChecker.*;

/**
 * @author cdfive
 */
public class ParamFlowRuleVoChecker {

    public static void checkAdd(AddParamFlowRuleReqVo reqVo) {
        checkNotNull(reqVo, "body");

        checkNotBlank(reqVo.getApp(), "app");

        checkNotBlank(reqVo.getResource(), "resource");

        checkNotNull(reqVo.getGrade(), "grade");
        checkCondition(reqVo.getGrade() == RuleConstant.FLOW_GRADE_QPS, "grade in parameter flow control must be qps" + RuleConstant.FLOW_GRADE_QPS);

        checkNotNull(reqVo.getParamIdx(), "paramIdx");
        checkCondition(reqVo.getParamIdx() >= 0, "paramIdx must be greater than or equal to 0");

        checkNotNull(reqVo.getCount(), "count");
        checkCondition(reqVo.getCount() >= 0, "count must be greater than or equal to 0");

        checkNotNull(reqVo.getDurationInSec(), "durationInSec");
        checkCondition(reqVo.getDurationInSec() >= 0, "durationInSec must be greater than or equal to 0");
    }

    public static void checkUpdate(UpdateParamFlowRuleReqVo reqVo) {
        checkNotNull(reqVo, "body");

        checkNotBlank(reqVo.getApp(), "app");

        checkNotNull(reqVo.getId(), "id");
        checkCondition(reqVo.getId() > 0, "id must be greater than 0");

        checkNotNull(reqVo.getGrade(), "grade");
        checkCondition(reqVo.getGrade() == RuleConstant.FLOW_GRADE_QPS, "grade in parameter flow control must be " + RuleConstant.FLOW_GRADE_QPS);

        checkNotNull(reqVo.getParamIdx(), "paramIdx");
        checkCondition(reqVo.getParamIdx() >= 0, "paramIdx must be greater than or equal to 0");

        checkNotNull(reqVo.getCount(), "count");
        checkCondition(reqVo.getCount() >= 0, "count must be greater than or equal to 0");

        checkNotNull(reqVo.getDurationInSec(), "durationInSec");
        checkCondition(reqVo.getDurationInSec() >= 0, "durationInSec must be greater than or equal to 0");
    }

    public static void checkDelete(DeleteParamFlowRuleReqVo reqVo) {
        checkNotNull(reqVo, "body");

        checkNotBlank(reqVo.getApp(), "app");

        checkNotNull(reqVo.getId(), "id");
        checkCondition(reqVo.getId() > 0, "id must be greater than 0");
    }
}
