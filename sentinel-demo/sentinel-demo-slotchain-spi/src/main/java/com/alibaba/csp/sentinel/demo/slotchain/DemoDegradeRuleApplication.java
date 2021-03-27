/*
 * Copyright 1999-2021 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.demo.slotchain;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.circuitbreaker.CircuitBreakerStrategy;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * Demo for degrade rule using custom SlotChainBuilder {@link DemoSlotChainBuilder}.
 *
 * You will see this in sentinel-record.log, indicating that the custom slot chain builder is activated:
 * [SlotChainProvider] Global slot chain builder resolved: com.alibaba.csp.sentinel.demo.slotchain.DemoSlotChainBuilder
 *
 * @author cdfive
 */
public class DemoDegradeRuleApplication {

    private static final String RESOURCE_KEY = "abc";

    public static void main(String[] args) throws Exception {
        initDegradeRule();

        for (int i = 1; i <= 100; i++) {
            Entry entry = null;
            try {
                entry = SphU.entry(RESOURCE_KEY);
                TimeUnit.MILLISECONDS.sleep(ThreadLocalRandom.current().nextInt(10, 100));
                System.out.println(i + "=>" + " passed");
            } catch (BlockException ex) {
                System.out.println(i + "=>" + " blocked by " + ex.getClass().getSimpleName());
            } finally {
                if (entry != null) {
                    entry.exit();
                }
            }
        }
    }

    private static void initDegradeRule() {
        List<DegradeRule> rules = new ArrayList<>();
        DegradeRule rule = new DegradeRule(RESOURCE_KEY)
                .setGrade(CircuitBreakerStrategy.SLOW_REQUEST_RATIO.getType())
                // Max allowed response time
                .setCount(20)
                // Retry timeout (in second)
                .setTimeWindow(10)
                // Circuit breaker opens when slow request ratio > 20%
                .setSlowRatioThreshold(0.2)
                .setMinRequestAmount(10)
                .setStatIntervalMs(20000);
        rules.add(rule);

        DegradeRuleManager.loadRules(rules);
        System.out.println("Degrade rule loaded: " + rules);
    }
}
