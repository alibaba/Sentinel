package com.alibaba.csp.sentinel.dashboard.rule.type.nacos;

import com.alibaba.csp.sentinel.dashboard.rule.AbstractRuleProvider;
import com.alibaba.nacos.api.config.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ResolvableType;

/**
 * @author cdfive
 */
//@ConditionalOnBean(NacosConfig.class)
//@ConditionalOnMissingBean(DynamicRuleProvider.class)
//@Component("nacosRuleProvider")
public class NacosRuleProvider<T> extends AbstractRuleProvider<T> {

    public NacosRuleProvider() {
//        Class clazz = (Class) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
//        System.out.println(clazz.getSimpleName());


//        Class clazz0 = (Class) ((ParameterizedType) this.getClass().getGenericInterfaces()[0]).getActualTypeArguments()[0];
//        Class clazz = (Class) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        Class clazz1 = ResolvableType.forClass(this.getClass()).getGeneric(0).resolve();
        System.out.println("nacosRuleProvider init");
    }

    @Autowired
    private ConfigService configService;

    @Override
    protected String fetchRules(String app, String ip, Integer port) throws Exception {
        String ruleKey = buildRuleKey(app, ip, port);
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
