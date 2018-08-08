/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.demo.dubbo.demo1;

import java.util.Collections;

import com.alibaba.csp.sentinel.init.InitExecutor;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Please add the following VM arguments:
 * <pre>
 * -Djava.net.preferIPv4Stack=true
 * -Dcsp.sentinel.api.port=8720
 * -Dproject.name=dubbo-provider-demo
 * </pre>
 *
 * @author Eric Zhao
 */
public class FooProviderBootstrap {

    private static final String RES_KEY = "com.alibaba.csp.sentinel.demo.dubbo.FooService:sayHello(java.lang.String)";
    private static final String INTERFACE_RES_KEY = "com.alibaba.csp.sentinel.demo.dubbo.FooService";

    public static void main(String[] args) {
        // Users don't need to manually call this method.
        InitExecutor.doInit();

        initFlowRule();

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
        context.register(ProviderConfiguration.class);
        context.refresh();

        System.out.println("Service provider is ready");
    }

    private static void initFlowRule() {
        FlowRule flowRule = new FlowRule();
        flowRule.setResource(RES_KEY);
        flowRule.setCount(10);
        flowRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        flowRule.setLimitApp("default");
        FlowRuleManager.loadRules(Collections.singletonList(flowRule));
    }
}
