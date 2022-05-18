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
package com.alibaba.csp.sentinel.demo.degrade.param;

import com.alibaba.csp.sentinel.slots.block.degrade.param.ParamDegradeItem;
import com.alibaba.csp.sentinel.slots.block.degrade.param.ParamDegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.param.ParamDegradeRuleManager;
import java.util.Collections;

/**
 * This demo demonstrates degrade by frequent ("hot spot") parameters.
 *
 */
public class ParamDegradeQpsDemo {

    private static final int PARAM_A = 1;
    private static final int PARAM_B = 2;
    private static final int PARAM_C = 3;
    private static final int PARAM_D = 4;

    /**
     * Here we prepare different parameters to validate flow control by parameters.
     */
    private static final Integer[] PARAMS = new Integer[] {PARAM_A, PARAM_B, PARAM_C, PARAM_D};

    private static final String RESOURCE_KEY = "resA";

    public static void main(String[] args) throws Exception {
        initParamDegradeRules();

        final int threadCount = 20;
        ParamDegradeQpsRunner<Integer> runner = new ParamDegradeQpsRunner<>(PARAMS, RESOURCE_KEY, threadCount, 120);
        runner.tick();

        Thread.sleep(1000);
        runner.simulateTraffic();
    }

    private static void initParamDegradeRules() {
        // Slow ratio mode, threshold is 1 for every frequent "hot spot" parameter in index 0 (the first arg).
        // Because the slow request cost more than 1000ms, so only several requests will pass, others will be blocked.
        final String resA = "resA";
        ParamDegradeRule ruleA = new ParamDegradeRule(resA).setParamIdx(0);
        ruleA.setCount(1d);
        ruleA.setSlowRatioThreshold(0.1d);
        ruleA.setTimeWindow(20);
        ruleA.setStatIntervalMs(20000);

        // We can set threshold count for specific parameter value individually.
        // Here we add an exception item. That means: QPS threshold of entries with parameter `PARAM_B` (type: int)
        // in index 0 will be 2000, rather than the global threshold (1).
        // The requests that param=1 will never be degraded.
        ParamDegradeItem item = new ParamDegradeItem();
        item.setObject("1");
        item.setCount(2000d);
        item.setClassType("java.util.String");

        ruleA.setParamDegradeItemList(Collections.singletonList(item));

        ParamDegradeRuleManager.loadRules(Collections.singletonList(ruleA));
    }
}
