package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.AuthorityRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.nacos.api.config.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author xiejj
 */
public class AuthorityRuleNacosPublisher extends AbstractDynamicRulePublisher<List<AuthorityRuleEntity>> {
    public AuthorityRuleNacosPublisher(Converter<List<AuthorityRuleEntity>,String> converter){
        super(NacosConfigUtil.AUTHORITY_RULE,converter);
    }
}
