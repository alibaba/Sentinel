package com.alibaba.csp.sentinel.dashboard.service.impl.rule.checker;

import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.system.AddSystemRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.system.DeleteSystemRuleReqVo;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.req.system.UpdateSystemRuleReqVo;

import static com.alibaba.csp.sentinel.dashboard.service.impl.common.ParamChecker.*;

/**
 * @author cdfive
 */
public class SystemRuleVoChecker {

    public static void checkAdd(AddSystemRuleReqVo reqVo) {
        checkNotNull(reqVo, "body");

        checkNotBlank(reqVo.getApp(), "app");
    }

    public static void checkUpdate(UpdateSystemRuleReqVo reqVo) {
        checkNotNull(reqVo, "body");

        checkNotBlank(reqVo.getApp(), "app");
    }

    public static void checkDelete(DeleteSystemRuleReqVo reqVo) {
        checkNotNull(reqVo, "body");

        checkNotNull(reqVo.getId(), "id");
        checkCondition(reqVo.getId() > 0, "id must be greater than 0");
    }
}
