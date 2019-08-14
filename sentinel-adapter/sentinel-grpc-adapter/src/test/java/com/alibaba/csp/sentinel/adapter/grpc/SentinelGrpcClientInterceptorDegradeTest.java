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

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.adapter.grpc.gen.FooRequest;
import com.alibaba.csp.sentinel.adapter.grpc.gen.FooResponse;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;

import io.grpc.StatusRuntimeException;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;

import static org.junit.Assert.*;

/**
 * @author zhengzechao
 */
public class SentinelGrpcClientInterceptorDegradeTest {

    private final String resourceName = "com.alibaba.sentinel.examples.FooService/helloWithEx";
    private final GrpcTestServer server = new GrpcTestServer();
    private final int timeWindow = 10;

    private void configureDegradeRule(int count) {
        DegradeRule rule = new DegradeRule()
            .setCount(count)
            .setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT)
            .setResource(resourceName)
            .setLimitApp("default")
            .as(DegradeRule.class)
            .setTimeWindow(timeWindow);
        DegradeRuleManager.loadRules(Collections.singletonList(rule));
    }

    private boolean sendRequest(FooServiceClient client) {
        try {
            FooResponse response = client.helloWithEx(FooRequest.newBuilder().setName("Sentinel").setId(666).build());
            System.out.println("Response: " + response);
            return true;
        } catch (StatusRuntimeException ex) {
            System.out.println("Blocked, cause: " + ex.getMessage());
            return false;
        }
    }

    @Test
    public void testGrpcClientInterceptor_degrade() throws IOException {
        final int port = 19316;

        configureDegradeRule(1);
        server.start(port, false);

        FooServiceClient client = new FooServiceClient("localhost", port, new SentinelGrpcClientInterceptor());

        assertFalse(sendErrorRequest(client));
        ClusterNode clusterNode = ClusterBuilderSlot.getClusterNode(resourceName, EntryType.OUT);
        assertNotNull(clusterNode);
        assertEquals(1, clusterNode.exceptionQps(), 0.01);
        // The second request will be blocked.
        assertFalse(sendRequest(client));
        assertEquals(1, clusterNode.blockRequest());

        server.stop();
    }

    private boolean sendErrorRequest(FooServiceClient client) {
        try {
            FooResponse response = client.helloWithEx(FooRequest.newBuilder().setName("Sentinel").setId(-1).build());
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
