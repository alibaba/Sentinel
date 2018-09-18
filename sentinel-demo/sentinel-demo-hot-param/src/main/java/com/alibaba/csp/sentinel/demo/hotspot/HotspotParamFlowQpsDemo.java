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
package com.alibaba.csp.sentinel.demo.hotspot;

import java.util.Collections;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.hotspot.HotItem;
import com.alibaba.csp.sentinel.slots.hotspot.HotParamRule;
import com.alibaba.csp.sentinel.slots.hotspot.HotParamRuleManager;

/**
 * This demo demonstrates flow control by frequent "hot" parameters.
 *
 * @author Eric Zhao
 * @since 0.2.0
 */
public class HotspotParamFlowQpsDemo {

    private static final int PARAM_A = 1;
    private static final int PARAM_B = 2;
    private static final int PARAM_C = 3;
    private static final int PARAM_D = 4;

    /**
     * Here we prepare different parameters to validate flow control by frequent "hot" parameters.
     */
    private static final Integer[] PARAMS = new Integer[] {PARAM_A, PARAM_B, PARAM_C, PARAM_D};

    private static final String RESOURCE_KEY = "resA";

    public static void main(String[] args) {
        initHotParamFlowRules();

        final int threadCount = 8;
        ParamFlowQpsRunner<Integer> runner = new ParamFlowQpsRunner<>(PARAMS, RESOURCE_KEY, threadCount, 60);
        runner.simulateTraffic();
        runner.tick();
    }

    private static void initHotParamFlowRules() {
        // QPS mode, threshold is 5 for every frequent "hot" parameter in index 0 (the first arg).
        HotParamRule rule = new HotParamRule()
            .setParamIdx(0)
            .setBlockGrade(RuleConstant.FLOW_GRADE_QPS)
            .setCount(5);
        // We can set threshold count for specific parameter value individually.
        // Here we add an exception item. That means: QPS threshold of entries with parameter `PARAM_B` (type: int)
        // in index 0 will be 10, rather than the global threshold (5).
        HotItem item = new HotItem().setObject(String.valueOf(PARAM_B))
            .setClassType(int.class.getName())
            .setCount(10);
        rule.setHotItemList(Collections.singletonList(item));
        rule.setResource(RESOURCE_KEY);
        HotParamRuleManager.loadRules(Collections.singletonList(rule));
    }
}
