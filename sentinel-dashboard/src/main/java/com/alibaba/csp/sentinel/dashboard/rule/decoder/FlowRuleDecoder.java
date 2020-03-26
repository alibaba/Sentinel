//package com.alibaba.csp.sentinel.dashboard.rule.decoder;
//
//import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
//import com.alibaba.csp.sentinel.dashboard.rule.DefaultRuleDecoder;
//import org.springframework.stereotype.Component;
//
//import java.lang.reflect.ParameterizedType;
//
///**
// * @author cdfive
// */
//@Component
//public class FlowRuleDecoder extends DefaultRuleDecoder<FlowRuleEntity> {
//
//    public FlowRuleDecoder() {
//        Class clazz = (Class) ((ParameterizedType) this.getClass().getGenericSuperclass()).getActualTypeArguments()[0];
//        System.out.println(clazz.getSimpleName());
//    }
//
//}
