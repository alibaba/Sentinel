package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.alibaba.csp.sentinel.datasource.Converter;

import java.util.List;

/**
 * @author xiejj
 */
public class DegradeRuleNacosPublisher extends AbstractDynamicRulePublisher<List<DegradeRuleEntity>> {
    public DegradeRuleNacosPublisher(Converter<List<DegradeRuleEntity>,String> converter){
        super(NacosConfigUtil.DEGRADE_RULE,converter);
    }
}
