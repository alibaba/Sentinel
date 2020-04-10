package com.alibaba.csp.sentinel.dashboard.repository.nacos;

import com.alibaba.csp.sentinel.dashboard.entity.rule.RuleEntity;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.csp.sentinel.dashboard.repository.AbstractRulePublisher;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author cdfive
 */
//@ConditionalOnBean(NacosConfig.class)
//@Component("nacosRulePublisher")
public class NacosRulePublisher<T extends RuleEntity> extends AbstractRulePublisher<T> {

    @Autowired
    private NacosProperties nacosProperties;

    @Autowired
    private ConfigService configService;

    @Override
    protected void publishRules(String app, String ip, Integer port, String rules) throws Exception {
        String ruleKey = buildRuleKey(app, ip, port);
        boolean result = configService.publishConfig(ruleKey, nacosProperties.getSentinelGroup(), rules);
        if (!result) {
            // TODO
        }
    }

//    @Autowired
//    private Converter<List<T>, String> converter;
//
//    @Override
//    public void publish(String app, String ip, Integer port, List<T> rules) throws Exception {
//        AssertUtil.notEmpty(app, "app name cannot be empty");
//
//        if (CollectionUtils.isEmpty(rules)) {
//            return;
//        }
//
//        boolean result = configService.publishConfig(NacosConfigUtil.buildMachineKey(app, ip, port, NacosConfigUtil.FLOW_DATA_ID_POSTFIX),
//                NacosConfigUtil.GROUP_ID, converter.convert(rules));
//        System.out.println(result);
//    }
}
