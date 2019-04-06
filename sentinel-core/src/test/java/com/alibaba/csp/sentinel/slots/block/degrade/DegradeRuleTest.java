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
package com.alibaba.csp.sentinel.slots.block.degrade;

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.node.Node;
import com.alibaba.csp.sentinel.node.StatisticNode;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author liufuguang.lao
 */
public class DegradeRuleTest {

    @Test
    public void testAverageRtDegradeByApp() throws InterruptedException {
        String key = "test_degrade_average_rt";

        Context context = mock(Context.class);
        DefaultNode node = mock(DefaultNode.class);

        StatisticNode originNode = new StatisticNode();
        when(context.getOriginNode()).thenReturn(originNode);

        DegradeRule rule = new DegradeRule();
        rule.setCount(1);
        rule.setResource(key);
        rule.setTimeWindow(5);
        rule.setLimitApp("origin");

        for (int i = 0; i < 4; i++) {
            originNode.addRtAndSuccess(2,1);
            assertTrue(rule.passCheck(context, node, 1));
        }

        // The third time will fail.
        originNode.addRtAndSuccess(2,1);
        assertFalse(rule.passCheck(context, node, 1));
        originNode.addRtAndSuccess(2,1);
        assertFalse(rule.passCheck(context, node, 1));

        // Restore.
        TimeUnit.SECONDS.sleep(6);
        assertTrue(rule.passCheck(context, node, 1));
    }

    @Test
    public void testExceptionRatioModeDegradeByApp() throws Throwable {
        String key = "test_degrade_exception_ratio";

        Context context = mock(Context.class);
        DefaultNode entranceNode = mock(DefaultNode.class);

        StatisticNode originNode = new StatisticNode();
        when(context.getOriginNode()).thenReturn(originNode);


        // Indicates that there are QPS more than min threshold.
        originNode.addRtAndSuccess(1,10);


        DegradeRule rule = new DegradeRule();
        rule.setCount(0.15);
        rule.setResource(key);
        rule.setTimeWindow(5);
        rule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
        rule.setLimitApp("origin");

        originNode.increaseExceptionQps(2);
        originNode.addPassRequest(10);
        // Will fail.
        assertFalse(rule.passCheck(context, entranceNode, 1));

        // Restore from the degrade timeout.
        TimeUnit.SECONDS.sleep(6);

        originNode.addRtAndSuccess(1,20);
        // Will pass.
        assertTrue(rule.passCheck(context, entranceNode, 1));
    }

    @Test
    public void testExceptionCountModeDegradeByApp() throws Throwable {
        String key = "test_degrade_exception_count";
        Context context = mock(Context.class);
        DefaultNode entranceNode = mock(DefaultNode.class);

        StatisticNode originNode = new StatisticNode();
        when(context.getOriginNode()).thenReturn(originNode);

        DegradeRule rule = new DegradeRule();
        rule.setCount(4);
        rule.setResource(key);
        rule.setTimeWindow(2);
        rule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT);
        rule.setLimitApp("origin");

        originNode.increaseExceptionQps(4);

        // Will fail.
        assertFalse(rule.passCheck(context, entranceNode, 1));

        // Restore from the degrade timeout.
        TimeUnit.SECONDS.sleep(3);

        rule.setCount(5);
        // Will pass.
        assertTrue(rule.passCheck(context, entranceNode, 1));
    }

}
