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
import com.alibaba.csp.sentinel.adapter.grpc.gen.FooRequest;
import com.alibaba.csp.sentinel.adapter.grpc.gen.FooResponse;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;

import io.grpc.StatusRuntimeException;
import org.junit.After;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;

/**
 * @author zhengzechao
 */
public class SentinelGrpcServerInterceptorDegradeTest {

    private final String resourceName = "com.alibaba.sentinel.examples.FooService/anotherHelloWithEx";
    private final GrpcTestServer server = new GrpcTestServer();
    private final int timeWindow = 10;
    private FooServiceClient client;

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

    private boolean sendRequest() {
        try {
            FooResponse response = client.anotherHelloWithEx(FooRequest.newBuilder().setName("Sentinel").setId(666)
                .build());
            System.out.println("Response: " + response);
            return true;
        } catch (StatusRuntimeException ex) {
            System.out.println("Blocked, cause: " + ex.getMessage());
            return false;
        }
    }

    @Test
    public void testGrpcServerInterceptor_degrade_fail_threads() throws IOException, InterruptedException {
        final int port = 19349;
        client = new FooServiceClient("localhost", port);
        server.start(port, true);
        // exception count  = 1
        configureDegradeRule(20);
        final CountDownLatch latch = new CountDownLatch(20);

        for (int i = 0; i < 20; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    assertFalse(sendErrorRequest());
                    latch.countDown();
                }
            }).start();
        }
        latch.await();
        assertFalse(sendRequest());
        ClusterNode clusterNode = ClusterBuilderSlot.getClusterNode(resourceName, EntryType.IN);
        assertEquals(20, clusterNode.totalException());
        assertEquals(1, clusterNode.blockRequest());

    }

    private boolean sendErrorRequest() {
        try {
            FooResponse response = client.anotherHelloWithEx(FooRequest.newBuilder().setName("Sentinel").setId(-1)
                .build());
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
