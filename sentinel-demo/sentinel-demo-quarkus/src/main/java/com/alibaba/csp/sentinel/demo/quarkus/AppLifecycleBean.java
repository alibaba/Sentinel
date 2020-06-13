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
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
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
        FlowRule rule = new FlowRule()
                .setCount(1)
                .setGrade(RuleConstant.FLOW_GRADE_QPS)
                .setResource("GET:/hello/txt")
                .setLimitApp("default")
                .as(FlowRule.class);
        FlowRuleManager.loadRules(Arrays.asList(rule));

        SystemRule systemRule = new SystemRule();
        systemRule.setLimitApp("default");
        systemRule.setAvgRt(3000);
        SystemRuleManager.loadRules(Arrays.asList(systemRule));

        DegradeRule degradeRule1 = new DegradeRule("greeting1")
                .setCount(1)
                .setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT)
                .setTimeWindow(10)
                .setMinRequestAmount(1);

        DegradeRule degradeRule2 = new DegradeRule("greeting2")
                .setCount(1)
                .setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT)
                .setTimeWindow(10)
                .setMinRequestAmount(1);
        DegradeRuleManager.loadRules(Arrays.asList(degradeRule1, degradeRule2));
    }

}
