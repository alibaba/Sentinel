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
package com.alibaba.csp.sentinel.slots.block.system;

import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * @author yanhom
 */
public class SystemRuleManagerTest {

    @Test
    public void testSystemRuleManagerCheckStatus() {
        SystemRule systemRule = new SystemRule();
        systemRule.setHighestCpuUsage(0.8);

        SystemRule systemRule1 = new SystemRule();

        List<SystemRule> systemRules = new ArrayList<>();
        systemRules.add(systemRule);
        systemRules.add(systemRule1);
        SystemRuleManager.loadRules(systemRules);
        boolean checkStatus = SystemRuleManager.getCheckSystemStatus();
        assertTrue(checkStatus);
    }

}
