package com.alibaba.csp.sentinel.dashboard.service.impl.rule.checker;

import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.flow.AddFlowRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.flow.DeleteFlowRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.flow.UpdateFlowRuleReqVo;

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
        checkInValues(reqVo.getGrade(), "grade",0, 1);

        checkNotNull(reqVo.getCount(), "count");
        checkCondition(reqVo.getCount() >= 0, "count must be at lease zero");

        checkNotNull(reqVo.getStrategy(), "strategy");
        if (reqVo.getStrategy() != 0) {
            checkNotBlankMessage(reqVo.getRefResource(), "refResource can't be null or empty when strategy!=0");
        }
//        checkCondition(reqVo.getStrategy() == 0 || !StringUtil.isBlank(reqVo.getRefResource()), "refResource can't be null or empty when strategy!=0");

        checkNotNull(reqVo.getControlBehavior(), "controlBehavior");
        if (reqVo.getControlBehavior() == 1) {
            checkNotNullMessage(reqVo.getWarmUpPeriodSec(), "warmUpPeriodSec can't be null when controlBehavior==1");
        }
        if (reqVo.getControlBehavior() == 2) {
            checkNotNullMessage(reqVo.getMaxQueueingTimeMs(), "maxQueueingTimeMs can't be null when controlBehavior==2");
        }

        if (reqVo.getClusterMode()) {
            checkNotNull(reqVo.getClusterConfig(), "clusterConfig");
        }
    }

    public static void checkUpdate(UpdateFlowRuleReqVo reqVo) {
        checkNotNull(reqVo, "body");

        checkNotBlank(reqVo.getApp(), "app");

        checkNotNull(reqVo.getId(), "id");
        checkCondition(reqVo.getId() > 0, "id must be greater than 0");

        checkNotBlank(reqVo.getLimitApp(), "limitApp");

        checkNotNull(reqVo.getGrade(), "grade");
        checkInValues(reqVo.getGrade(), "grade",0, 1);

        checkNotNull(reqVo.getCount(), "count");
        checkCondition(reqVo.getCount() >= 0, "count must be greater than or equal to 0");

        checkNotNull(reqVo.getStrategy(), "strategy");
        if (reqVo.getStrategy() != 0) {
            checkNotBlankMessage(reqVo.getRefResource(), "refResource can't be null or empty when strategy!=0");
        }

        checkNotNull(reqVo.getControlBehavior(), "controlBehavior");
        if (reqVo.getControlBehavior() == 1) {
            checkNotNullMessage(reqVo.getWarmUpPeriodSec(), "warmUpPeriodSec can't be null when controlBehavior==1");
        }
        if (reqVo.getControlBehavior() == 2) {
            checkNotNullMessage(reqVo.getMaxQueueingTimeMs(), "maxQueueingTimeMs can't be null when controlBehavior==2");
        }

        if (reqVo.getClusterMode()) {
            checkNotNull(reqVo.getClusterConfig(), "clusterConfig");
        }
    }

    public static void checkDelete(DeleteFlowRuleReqVo reqVo) {
        checkNotNull(reqVo, "body");

        checkNotNull(reqVo.getId(), "id");
        checkCondition(reqVo.getId() > 0, "id must be greater than 0");
    }
}
