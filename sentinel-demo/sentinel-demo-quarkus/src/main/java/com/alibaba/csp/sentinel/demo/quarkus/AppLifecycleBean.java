/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.demo.quarkus;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreakerStrategy;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;

import io.quarkus.runtime.StartupEvent;
import org.jboss.logging.Logger;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;

import java.util.Arrays;

/**
 * @author sea
 */
@ApplicationScoped
public class AppLifecycleBean {

    private static final Logger LOGGER = Logger.getLogger("ListenerBean");

    void onStart(@Observes StartupEvent ev) {
        LOGGER.info("The application is starting...");

        // Only for test here. Actually it's recommended to configure rules via data-source.
        FlowRule rule1 = new FlowRule()
            .setCount(1)
            .setGrade(RuleConstant.FLOW_GRADE_QPS)
            .setResource("GET:/hello/txt")
            .setLimitApp("default")
            .as(FlowRule.class);
        FlowRule rule2 = new FlowRule("greeting2")
            .setCount(1)
            .setGrade(RuleConstant.FLOW_GRADE_QPS)
            .as(FlowRule.class);
        FlowRuleManager.loadRules(Arrays.asList(rule1, rule2));

        DegradeRule degradeRule1 = new DegradeRule("greeting1")
            .setCount(1)
            .setGrade(CircuitBreakerStrategy.ERROR_COUNT.getType())
            .setTimeWindow(5)
            .setStatIntervalMs(10000)
            .setMinRequestAmount(1);

        DegradeRuleManager.loadRules(Arrays.asList(degradeRule1));
    }

}
