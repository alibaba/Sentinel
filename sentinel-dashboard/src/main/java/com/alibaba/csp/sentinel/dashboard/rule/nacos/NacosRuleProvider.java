package com.alibaba.csp.sentinel.dashboard.rule.nacos;

import com.alibaba.csp.sentinel.dashboard.rule.AbstractRuleProvider;
import com.alibaba.nacos.api.config.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

/**
 * @author cdfive
 */
@Primary
@ConditionalOnBean(NacosConfig.class)
@Component("nacosRuleProvider")
public class NacosRuleProvider<T> extends AbstractRuleProvider<T> {

    public NacosRuleProvider() {
        System.out.println("nacosRuleProvider init");
    }

    @Autowired
    private ConfigService configService;

    @Override
    protected String fetchRules(String ruleKey) throws Exception {
        return configService.getConfig(ruleKey, NacosConfigUtil.GROUP_ID, 3000);
    }

//    @Autowired
//    private Converter<String, List<T>> converter;

//    @Override
//    public List<T> getRules(String app, String ip, Integer port) throws Exception {
//        String rules = configService.getConfig(NacosConfigUtil.buildMachineKey(app, ip, port, NacosConfigUtil.FLOW_DATA_ID_POSTFIX),
//                NacosConfigUtil.GROUP_ID, 3000);
//
//        if (StringUtil.isEmpty(rules)) {
//            return new ArrayList<>();
//        }
//
//        return converter.convert(rules);
//    }
}
