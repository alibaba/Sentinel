package com.alibaba.csp.sentinel.annotation.aspectj;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.annotation.aspectj.annotation.DegradeRuleDefine;
import com.alibaba.csp.sentinel.annotation.aspectj.annotation.FlowRuleDefine;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class SentinelAnnotationBeanProcessor implements BeanPostProcessor, ApplicationListener<ContextRefreshedEvent> {

    List<FlowRule> flowRules = new ArrayList<FlowRule>();
    List<DegradeRule> degradeRules = new ArrayList<DegradeRule>();

    private String flowGradeString(int grade) {
        switch (grade) {
            case 0: return "FLOW_GRADE_THREAD";
            case 1: return "FLOW_GRADE_QPS";
            default: return "unknown flow grade";
        }
    }

    private String degradeGradeString(int grade) {
        switch (grade) {
            case 0: return "DEGRADE_GRADE_RT";
            case 1: return "DEGRADE_GRADE_EXCEPTION_RATIO";
            case 2: return "DEGRADE_GRADE_EXCEPTION_COUNT";
            default: return "unknown degrade grade";
        }
    }

    private String behaviorGradeString(int behavior) {
        switch (behavior) {
            case 0: return "CONTROL_BEHAVIOR_DEFAULT";
            case 1: return "CONTROL_BEHAVIOR_WARM_UP";
            case 2: return "CONTROL_BEHAVIOR_RATE_LIMITER";
            case 3: return "CONTROL_BEHAVIOR_WARM_UP_RATE_LIMITER";
            default: return "unknown behavior grade";
        }
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    private boolean isHandlerDefined(String handler) {
        return handler != null && !handler.isEmpty();
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        //logger.info("{} => {}", bean.getClass().getSimpleName(), AopUtils.getTargetClass(bean).getSimpleName());

        String className = AopUtils.getTargetClass(bean).getCanonicalName();

        for (Method method : AopUtils.getTargetClass(bean).getDeclaredMethods()) {
            if (method.isAnnotationPresent(SentinelResource.class)) {

                SentinelResource annotationSentinel = method.getAnnotation(SentinelResource.class);

                if (isHandlerDefined(annotationSentinel.blockHandler())) {
                    FlowRuleDefine flowDef = method.getAnnotation(FlowRuleDefine.class);
                    if (flowDef != null) {
                        FlowRule rule = new FlowRule();
                        rule.setResource(annotationSentinel.value());
                        rule.setCount(flowDef.count());
                        rule.setGrade(flowDef.grade());
                        rule.setControlBehavior(flowDef.behavior());
                        flowRules.add(rule);
                        RecordLog.info("SENTINEL   FLOW  ==> {0}.{1}() withResourceName {2} = (grade:{3}, count:{4}, behavior:{5})",
                                className, method.getName(), annotationSentinel.value(),
                                flowGradeString(flowDef.grade()), flowDef.count(), behaviorGradeString(flowDef.behavior()));
                    } else {
                        //RecordLog.warn("SENTINEL   FLOW  ==> not found @FlowRuleDefine at [{0}.{1}], remove `blockHandler` or add @FlowRuleDefine",
                        //        className, method.getName());
                    }
                }

                if (isHandlerDefined(annotationSentinel.fallback()) ||
                        isHandlerDefined(annotationSentinel.defaultFallback())) {
                    DegradeRuleDefine degradeDef = method.getAnnotation(DegradeRuleDefine.class);
                    if (degradeDef != null) {
                        DegradeRule rule = new DegradeRule();
                        rule.setResource(annotationSentinel.value());
                        rule.setCount(degradeDef.count());
                        rule.setGrade(degradeDef.grade());
                        rule.setTimeWindow(degradeDef.timeWindow());
                        degradeRules.add(rule);
                        RecordLog.info("SENTINEL DEGRADE ==> {0}.{1}() withResourceName {2} = (grade:{3}, count:{4}, timeWindow:{5}(s))",
                                className, method.getName(), annotationSentinel.value(),
                                degradeGradeString(degradeDef.grade()), degradeDef.count(), degradeDef.timeWindow());
                    } else {
                        //RecordLog.warn("SENTINEL DEGRADE ==> not found @DegradeRuleDefine at [{0}.{1}], remove `fallback` or add @DegradeRuleDefine",
                        //        className, method.getName());
                    }
                }
            }
        }
        return bean;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        RecordLog.info("SENTINEL         ==> load {0} flow rules", flowRules.size());
        FlowRuleManager.loadRules(flowRules);
        RecordLog.info("SENTINEL         ==> load {0} degrade rules", degradeRules.size());
        DegradeRuleManager.loadRules(degradeRules);
    }
}
