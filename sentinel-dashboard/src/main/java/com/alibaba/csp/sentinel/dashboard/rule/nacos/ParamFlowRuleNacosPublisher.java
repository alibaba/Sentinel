package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.alibaba.csp.sentinel.datasource.Converter;

import java.util.List;

/**
 * @author xiejj
 */
public class ParamFlowRuleNacosPublisher extends AbstractDynamicRulePublisher<List<ParamFlowRuleEntity>> {
    public ParamFlowRuleNacosPublisher(Converter<List<ParamFlowRuleEntity>,String> converter){
        super(NacosConfigUtil.PARAM_RULE,converter);
    }
}
