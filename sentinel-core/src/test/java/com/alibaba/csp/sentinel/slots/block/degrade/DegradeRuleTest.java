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
 * @author liufuguang.lao
 */
public class DegradeRuleTest {

    @Test
    public void testDegradeByOrigin() throws InterruptedException {
        String key = "test_degrade_by_origin";
        ClusterNode cn = mock(ClusterNode.class);
        ClusterBuilderSlot.getClusterNodeMap().put(new StringResourceWrapper(key, EntryType.IN), cn);

        ClusterNode originNode = new ClusterNode();
        originNode.addRtAndSuccess(2, 1);
        originNode.increaseExceptionQps(3);


        Context context = mock(Context.class);
        when(context.getOriginNode()).thenReturn(originNode);

        DefaultNode node = mock(DefaultNode.class);


        DegradeRule rule = new DegradeRule();
        rule.setCount(4);
        rule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT);
        rule.setResource(key);
        rule.setTimeWindow(5);
        rule.setLimitApp("origin");


        for (int i = 0; i < 4; i++) {
            assertTrue(rule.passCheck(context, node, 1));
        }
        originNode.increaseExceptionQps(1);
        // The third time will fail.
        assertFalse(rule.passCheck(context, node, 1));
        assertFalse(rule.passCheck(context, node, 1));
    }

    @Test
    public void testDegradeByResource() throws InterruptedException {
        String key = "test_degrade_by_resource";
        ClusterNode cn = mock(ClusterNode.class);
        ClusterBuilderSlot.getClusterNodeMap().put(new StringResourceWrapper(key, EntryType.IN), cn);

        Context context = mock(Context.class);

        DefaultNode node = mock(DefaultNode.class);
        when(node.getClusterNode()).thenReturn(cn);
        when(cn.totalException()).thenReturn(5L);

        DegradeRule rule = new DegradeRule();
        rule.setCount(5);
        rule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT);
        rule.setResource(key);
        rule.setTimeWindow(5);
        rule.setLimitApp("default");

        for (int i = 0; i < 4; i++) {
            assertTrue(rule.passCheck(context, node, 1));
        }

        // The third time will fail.
        assertFalse(rule.passCheck(context, node, 1));
        assertFalse(rule.passCheck(context, node, 1));

        // Restore.
        TimeUnit.SECONDS.sleep(6);
        assertTrue(rule.passCheck(context, node, 1));
    }

}
