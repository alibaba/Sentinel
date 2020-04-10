package com.alibaba.csp.sentinel.dashboard.repository.encoder;

import com.alibaba.csp.sentinel.dashboard.entity.rule.FlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.repository.DefaultRuleEncoder;
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
