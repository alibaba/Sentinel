package com.alibaba.csp.sentinel.dashboard.rule.type.nacos.provider;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.type.nacos.NacosConfig;
import com.alibaba.csp.sentinel.dashboard.rule.type.nacos.NacosRuleProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * @author cdfive
 */
@ConditionalOnBean(NacosConfig.class)
//@Primary
@Component
public class NacosFlowRuleProvider extends NacosRuleProvider<FlowRuleEntity> {

    public NacosFlowRuleProvider() {
//        Class clazz = (Class) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
//        Class clazz1 = ResolvableType.forClass(this.getClass()).getGeneric(0).resolve();
        System.out.println("NacosFlowRuleProvider init");
    }
}
