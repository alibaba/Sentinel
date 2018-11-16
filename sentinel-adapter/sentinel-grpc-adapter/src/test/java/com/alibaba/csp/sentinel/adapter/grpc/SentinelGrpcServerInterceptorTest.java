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
package com.alibaba.csp.sentinel.adapter.grpc;

import java.util.Collections;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.adapter.grpc.gen.FooRequest;
import com.alibaba.csp.sentinel.adapter.grpc.gen.FooResponse;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;

import io.grpc.StatusRuntimeException;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Test cases for {@link SentinelGrpcServerInterceptor}.
 *
 * @author Eric Zhao
 */
public class SentinelGrpcServerInterceptorTest {

    private final String resourceName = "com.alibaba.sentinel.examples.FooService/anotherHello";
    private final int threshold = 4;
    private final GrpcTestServer server = new GrpcTestServer();

    private FooServiceClient client;

    private void configureFlowRule(int count) {
        FlowRule rule = new FlowRule()
            .setCount(count)
            .setGrade(RuleConstant.FLOW_GRADE_QPS)
            .setResource(resourceName)
            .setLimitApp("default")
            .as(FlowRule.class);
        FlowRuleManager.loadRules(Collections.singletonList(rule));
    }

    @Test
    public void testGrpcServerInterceptor() throws Exception {
        final int port = 19329;
        client = new FooServiceClient("localhost", port);

        configureFlowRule(threshold);
        server.start(port, true);

        assertTrue(sendRequest());
        ClusterNode clusterNode = ClusterBuilderSlot.getClusterNode(resourceName, EntryType.IN);
        assertNotNull(clusterNode);
        assertEquals(1, clusterNode.totalRequest() - clusterNode.blockRequest());

        // Not allowed to pass.
        configureFlowRule(0);

        // The second request will be blocked.
        assertFalse(sendRequest());
        assertEquals(1, clusterNode.blockRequest());

        server.stop();
    }

    private boolean sendRequest() {
        try {
            FooResponse response = client.anotherHello(FooRequest.newBuilder().setName("Sentinel").setId(666).build());
            System.out.println("Response: " + response);
            return true;
        } catch (StatusRuntimeException ex) {
            System.out.println("Blocked, cause: " + ex.getMessage());
            return false;
        }
    }


    @After
    public void cleanUp() {
        FlowRuleManager.loadRules(null);
        ClusterBuilderSlot.getClusterNodeMap().clear();
    }
}