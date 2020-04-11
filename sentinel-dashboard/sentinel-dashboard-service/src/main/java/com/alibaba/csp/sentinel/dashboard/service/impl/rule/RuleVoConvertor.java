package com.alibaba.csp.sentinel.dashboard.service.impl.rule;

import com.alibaba.csp.sentinel.dashboard.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.service.vo.rule.resp.flow.QueryFlowRuleListRespVo;

/**
 * @author cdfive
 */
public class RuleVoConvertor {

    public static QueryFlowRuleListRespVo convert(FlowRuleEntity entity) {
        QueryFlowRuleListRespVo vo = new QueryFlowRuleListRespVo();
        return vo;
    }
}
