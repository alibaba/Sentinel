package com.alibaba.csp.sentinel.dashboard.service.impl.rule.checker;

import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.degrade.AddDegradeRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.degrade.DeleteDegradeRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.degrade.UpdateDegradeRuleReqVo;

import static com.alibaba.csp.sentinel.dashboard.service.impl.common.ParamChecker.*;

public class DegradeRuleVoChecker {

    public static void checkAdd(AddDegradeRuleReqVo reqVo) {
        checkNotNull(reqVo, "body");

        checkNotBlank(reqVo.getResource(), "resource");

        checkNotNull(reqVo.getGrade(), "grade");
        checkInValues(reqVo.getGrade(), "grade",0, 1, 2);

        checkNotNull(reqVo.getCount(), "count");
        checkCondition(reqVo.getCount() >= 0, "count must be at lease 0");

        checkNotNull(reqVo.getTimeWindow(), "timeWindow");
        checkCondition(reqVo.getTimeWindow() > 0, "timeWindow must be greater than zero");
    }

    public static void checkUpdate(UpdateDegradeRuleReqVo reqVo) {
        checkNotNull(reqVo, "body");

        checkNotNull(reqVo.getGrade(), "grade");
        checkInValues(reqVo.getGrade(), "grade",0, 1, 2);

        checkNotNull(reqVo.getCount(), "count");
        checkCondition(reqVo.getCount() >= 0, "count must be at lease zero");

        checkNotNull(reqVo.getTimeWindow(), "timeWindow");
        checkCondition(reqVo.getTimeWindow() > 0, "timeWindow must be greater than zero");
    }

    public static void checkDelete(DeleteDegradeRuleReqVo reqVo) {
        checkNotNull(reqVo, "body");

        checkNotNull(reqVo.getId(), "id");
        checkCondition(reqVo.getId() > 0, "id must be greater than 0");
    }
}
