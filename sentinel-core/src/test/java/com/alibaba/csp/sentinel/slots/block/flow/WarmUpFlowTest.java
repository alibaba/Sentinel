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
package com.alibaba.csp.sentinel.slots.block.flow;

import java.util.Arrays;

import org.junit.Test;

import com.alibaba.csp.sentinel.context.ContextUtil;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;

/**
 * @author jialiang.linjl
 */
public class WarmUpFlowTest {

    @Test
    public void testWarmupFlowControl() {
        FlowRule flowRule = new FlowRule();
        flowRule.setResource("testWarmupFlowControl");
        flowRule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        flowRule.setCount(10);
        flowRule.setStrategy(RuleConstant.STRATEGY_DIRECT);
        flowRule.setWarmUpPeriodSec(10);
        flowRule.setControlBehavior(RuleConstant.CONTROL_BEHAVIOR_WARM_UP);

        FlowRuleManager.loadRules(Arrays.asList(flowRule));

        //ContextUtil.enter(null);

        //when(flowRule.selectNodeByRequsterAndStrategy(null, null, null)).thenReturn(value)

        // flowRule.passCheck(null, DefaultNode, acquireCount, args)
        // when(leapArray.values()).thenReturn(new ArrayList<Window>() {{ add(windowWrap.value()); }});
        ContextUtil.enter("test");

        ContextUtil.exit();

    }

}
