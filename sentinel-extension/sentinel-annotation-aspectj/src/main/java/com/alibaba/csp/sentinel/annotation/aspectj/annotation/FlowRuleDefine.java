package com.alibaba.csp.sentinel.annotation.aspectj.annotation;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Inherited
@Documented
public @interface FlowRuleDefine {
    int count() default 20;
    int grade() default RuleConstant.FLOW_GRADE_QPS;
    int behavior() default RuleConstant.CONTROL_BEHAVIOR_DEFAULT;
}
