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
 * @date 2018/12/7
 * Email ooczzoo@gmail.com
 */
public class SentinelGrpcClientInterceptorDegradeTest {

    private final String resourceName = "com.alibaba.sentinel.examples.FooService/helloWithEx";
    private final int threshold = 2;
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
        assertEquals(1, clusterNode.exceptionQps());
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
