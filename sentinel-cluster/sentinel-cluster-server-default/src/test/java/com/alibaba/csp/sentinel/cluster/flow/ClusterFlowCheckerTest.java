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
package com.alibaba.csp.sentinel.cluster.flow;

import java.util.ArrayList;

import com.alibaba.csp.sentinel.cluster.TokenResult;
import com.alibaba.csp.sentinel.cluster.flow.rule.ClusterFlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.alibaba.csp.sentinel.cluster.ClusterFlowTestUtil.*;

/**
 * @author Eric Zhao
 * @since 1.4.0
 */
public class ClusterFlowCheckerTest {

    @Before
    public void setUp() throws Exception {
        ClusterFlowRuleManager.removeProperty(TEST_NAMESPACE);
        ClusterFlowRuleManager.loadRules(TEST_NAMESPACE, new ArrayList<FlowRule>());
    }

    @After
    public void tearDown() throws Exception {
        ClusterFlowRuleManager.removeProperty(TEST_NAMESPACE);
        ClusterFlowRuleManager.loadRules(TEST_NAMESPACE, new ArrayList<FlowRule>());
    }

    private TokenResult tryAcquire(FlowRule clusterRule, boolean occupy) {
        return ClusterFlowChecker.acquireClusterToken(clusterRule, 1, occupy);
    }
}
