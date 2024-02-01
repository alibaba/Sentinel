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

import com.alibaba.csp.sentinel.context.Context;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.node.DefaultNode;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.system.SystemMetricType;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import org.junit.Test;

import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author jialiang.linjl
 * @author guozhong.huang
 */
public class SystemRuleTest {

    @Test
    public void testSystemRule_load() {
        SystemRule systemRule = new SystemRule();

        systemRule.setTriggerCount(4000L);
        systemRule.setSystemMetricType(SystemMetricType.AVG_RT);

        SystemRuleManager.loadRules(Collections.singletonList(systemRule));
    }

    @Test
    public void testSystemRule_avgRt() throws BlockException {

        SystemRule systemRule = new SystemRule();

        systemRule.setTriggerCount(4L);
        systemRule.setSystemMetricType(SystemMetricType.AVG_RT);

        Context context = mock(Context.class);
        DefaultNode node = mock(DefaultNode.class);
        ClusterNode cn = mock(ClusterNode.class);

        when(context.getOrigin()).thenReturn("");
        when(node.getClusterNode()).thenReturn(cn);

    }

}
