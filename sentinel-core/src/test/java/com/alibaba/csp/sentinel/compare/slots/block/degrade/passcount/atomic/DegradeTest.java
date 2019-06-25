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
package com.alibaba.csp.sentinel.compare.slots.block.degrade.passcount.atomic;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slotchain.StringResourceWrapper;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;
import com.alibaba.csp.sentinel.util.TimeUtil;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author Jobs Wang
 */
public class DegradeTest {

    @Test
    public void testAverageRtDegrade() throws InterruptedException {
        String key = "test_degrade_average_rt";
        ClusterNode cn = mock(ClusterNode.class);
        ClusterBuilderSlot.getClusterNodeMap().put(new StringResourceWrapper(key, EntryType.IN), cn);

        final Context context = mock(Context.class);
        final DefaultNode node = mock(DefaultNode.class);
        when(node.getClusterNode()).thenReturn(cn);
        when(cn.avgRt()).thenReturn(2d);

        int rtSlowRequestAmount = 10000;
        final DegradeRule rule = new DegradeRule();
        rule.setCount(1);
        rule.setResource(key);
        rule.setTimeWindow(2);
        rule.setGrade(RuleConstant.DEGRADE_GRADE_RT);
        rule.setRtSlowRequestAmount(rtSlowRequestAmount);

        final int nThreads = 8000;
        final CountDownLatch latch = new CountDownLatch(nThreads);
        final long time = TimeUtil.currentTimeMillis();

        Runnable task = new Runnable() {
            @Override
            public void run() {
                rule.passCheck(context, node, 1);
                latch.countDown();
            }
        };

        for (int i = 0; i < nThreads; i++) {
            new Thread(task).start();
        }

        latch.await();

        assertEquals(nThreads, rule.getPassCount());
        System.out.format("Run Degrade passCheck with Atomic passCount: %dms", TimeUtil.currentTimeMillis() - time);
    }

}
