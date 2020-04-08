package com.alibaba.csp.sentinel.dashboard.rule.encoder;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DefaultRuleEncoder;
import org.springframework.stereotype.Component;

import java.lang.reflect.ParameterizedType;

/**
 * @author cdfive
 */
@Component
public class FlowRuleEncoder extends DefaultRuleEncoder<FlowRuleEntity> {

    public FlowRuleEncoder() {
        Class clazz = (Class) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
        System.out.println(clazz.getSimpleName());
    }
}
