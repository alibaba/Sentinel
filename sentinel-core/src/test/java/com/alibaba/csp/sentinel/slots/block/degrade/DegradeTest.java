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

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.StringResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author jialiang.linjl
 */
public class DegradeTest {

    @Test
    public void testAverageRtDegrade() throws InterruptedException {
        String key = "test_degrade_average_rt";
        ClusterNode cn = mock(ClusterNode.class);
        ClusterBuilderSlot.getClusterNodeMap().put(new StringResourceWrapper(key, EntryType.IN), cn);

        Context context = mock(Context.class);
        DefaultNode node = mock(DefaultNode.class);
        when(node.getClusterNode()).thenReturn(cn);
        when(cn.avgRt()).thenReturn(2d);

        int rtSlowRequestAmount = 10;
        DegradeRule rule = new DegradeRule();
        rule.setCount(1);
        rule.setResource(key);
        rule.setTimeWindow(2);
        rule.setGrade(RuleConstant.DEGRADE_GRADE_RT);
        rule.setRtSlowRequestAmount(rtSlowRequestAmount);

        //Will true
        for (int i = 0; i < rtSlowRequestAmount - 1; i++) {
            assertTrue(rule.passCheck(context, node, 1));
        }

        // The third time will fail.
        assertFalse(rule.passCheck(context, node, 1));
        assertFalse(rule.passCheck(context, node, 1));

        // Restore.
        TimeUnit.MILLISECONDS.sleep(2200);
        assertTrue(rule.passCheck(context, node, 1));
    }

    @Test
    public void testExceptionRatioModeDegrade() throws Throwable {
        String key = "test_degrade_exception_ratio";
        ClusterNode cn = mock(ClusterNode.class);
        ClusterBuilderSlot.getClusterNodeMap().put(new StringResourceWrapper(key, EntryType.IN), cn);

        Context context = mock(Context.class);
        DefaultNode node = mock(DefaultNode.class);
        when(node.getClusterNode()).thenReturn(cn);

        DegradeRule rule = new DegradeRule();
        rule.setCount(0.15);
        rule.setResource(key);
        rule.setTimeWindow(2);
        rule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
        rule.setMinRequestAmount(20);


        // Will true. While totalQps < minRequestAmount
        when(cn.totalQps()).thenReturn(8d);
        assertTrue(rule.passCheck(context, node, 1));

        // Will true.
        when(cn.totalQps()).thenReturn(21d);
        when(cn.successQps()).thenReturn(9d);
        when(cn.exceptionQps()).thenReturn(9d);
        assertTrue(rule.passCheck(context, node, 1));


        // Will true. While totalQps > minRequestAmount and  exceptionRation < count
        when(cn.totalQps()).thenReturn(100d);
        when(cn.successQps()).thenReturn(90d);
        when(cn.exceptionQps()).thenReturn(10d);
        assertTrue(rule.passCheck(context, node, 1));

        // Will fail. While totalQps > minRequestAmount and exceptionRation > count
        rule.setMinRequestAmount(5);
        when(cn.totalQps()).thenReturn(12d);
        when(cn.successQps()).thenReturn(8d);
        when(cn.exceptionQps()).thenReturn(6d);
        assertFalse(rule.passCheck(context, node, 1));

        // Restore from the degrade timeout.
        TimeUnit.MILLISECONDS.sleep(2200);

        // Will pass.
        when(cn.totalQps()).thenReturn(106d);
        when(cn.successQps()).thenReturn(100d);
        assertTrue(rule.passCheck(context, node, 1));
    }

    @Test
    public void testExceptionCountModeDegrade() throws Throwable {
        String key = "test_degrade_exception_count";
        ClusterNode cn = mock(ClusterNode.class);
        when(cn.totalException()).thenReturn(10L);
        ClusterBuilderSlot.getClusterNodeMap().put(new StringResourceWrapper(key, EntryType.IN), cn);

        Context context = mock(Context.class);
        DefaultNode node = mock(DefaultNode.class);
        when(node.getClusterNode()).thenReturn(cn);

        DegradeRule rule = new DegradeRule();
        rule.setCount(4);
        rule.setResource(key);
        rule.setTimeWindow(2);
        rule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT);

        when(cn.totalException()).thenReturn(4L);

        // Will fail.
        assertFalse(rule.passCheck(context, node, 1));

        // Restore from the degrade timeout.
        TimeUnit.MILLISECONDS.sleep(2200);

        when(cn.totalException()).thenReturn(0L);
        // Will pass.
        assertTrue(rule.passCheck(context, node, 1));
    }

    @Test
    public void testEquals() {
        DegradeRule degradeRule1 = new DegradeRule();
        DegradeRule degradeRule2 = new DegradeRule();
        assertTrue(degradeRule1.equals(degradeRule2));

        int rtSlowRequestAmount = 10;
        int minRequestAmount = 20;
        double count = 1.0;
        int timeWindow = 2;
        degradeRule1.setRtSlowRequestAmount(rtSlowRequestAmount);
        degradeRule1.setMinRequestAmount(minRequestAmount);
        degradeRule1.setCount(count);
        degradeRule1.setTimeWindow(timeWindow);
        degradeRule1.setGrade(RuleConstant.DEGRADE_GRADE_RT);

        degradeRule2.setRtSlowRequestAmount(rtSlowRequestAmount);
        degradeRule2.setMinRequestAmount(minRequestAmount);
        degradeRule2.setCount(count);
        degradeRule2.setGrade(RuleConstant.DEGRADE_GRADE_RT);
        degradeRule2.setTimeWindow(timeWindow);
        assertTrue(degradeRule1.equals(degradeRule2));

        degradeRule2.setMinRequestAmount(100);
        assertFalse(degradeRule1.equals(degradeRule2));


    }

}
