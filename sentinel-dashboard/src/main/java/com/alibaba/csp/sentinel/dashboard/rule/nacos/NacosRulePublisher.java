package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.csp.sentinel.dashboard.rule.AbstractRulePublisher;
import com.alibaba.nacos.api.config.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * @author cdfive
 */
@ConditionalOnBean(NacosConfig.class)
@Component("nacosRulePublisher")
public class NacosRulePublisher<T> extends AbstractRulePublisher<T> {

    @Autowired
    private ConfigService configService;

    @Override
    protected void publishRules(String app, String ip, Integer port, String rules) throws Exception {
        String ruleKey = buildRuleKey(app, ip, port);
        configService.publishConfig(ruleKey, NacosConfigUtil.GROUP_ID, rules);
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
