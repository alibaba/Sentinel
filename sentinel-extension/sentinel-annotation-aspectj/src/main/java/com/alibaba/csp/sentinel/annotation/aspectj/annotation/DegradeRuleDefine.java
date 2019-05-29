package com.alibaba.csp.sentinel.annotation.aspectj.annotation;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
@Inherited
@Documented
public @interface DegradeRuleDefine {
    int timeWindow() default 10; // seconds
    int count() default 20;
    int grade() default RuleConstant.DEGRADE_GRADE_RT;
}
