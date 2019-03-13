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

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.junit.Test;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.StringResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;

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

        DegradeRule rule = new DegradeRule();
        rule.setCount(1);
        rule.setResource(key);
        rule.setTimeWindow(2);

        for (int i = 0; i < 4; i++) {
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
        when(cn.exceptionQps()).thenReturn(2d);
        // Indicates that there are QPS more than min threshold.
        when(cn.totalQps()).thenReturn(12d);
        ClusterBuilderSlot.getClusterNodeMap().put(new StringResourceWrapper(key, EntryType.IN), cn);

        Context context = mock(Context.class);
        DefaultNode node = mock(DefaultNode.class);
        when(node.getClusterNode()).thenReturn(cn);

        DegradeRule rule = new DegradeRule();
        rule.setCount(0.15);
        rule.setResource(key);
        rule.setTimeWindow(2);
        rule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);

        when(cn.successQps()).thenReturn(8d);

        // Will fail.
        assertFalse(rule.passCheck(context, node, 1));

        // Restore from the degrade timeout.
        TimeUnit.MILLISECONDS.sleep(2200);

        when(cn.successQps()).thenReturn(20d);
        // Will pass.
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

}
