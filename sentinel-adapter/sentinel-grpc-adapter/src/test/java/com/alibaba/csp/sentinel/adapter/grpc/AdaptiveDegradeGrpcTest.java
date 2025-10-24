package com.alibaba.csp.sentinel.adapter.grpc;

import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.adapter.grpc.gen.FooRequest;
import com.alibaba.csp.sentinel.adapter.grpc.gen.FooResponse;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.AdaptiveDegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.adaptive.AdaptiveServerMetric;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;
import io.grpc.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

/**
 * Unit test for Adaptive degradation in gRPC adapter.
 *
 * @author ylnxwlp
 */
public class AdaptiveDegradeGrpcTest {

    private static final String RESOURCE_NAME = "com.alibaba.sentinel.examples.FooService/sayHello";
    private final GrpcTestServer server = new GrpcTestServer();
    private FooServiceClient client;

    @Before
    public void setUp() {
        ClusterBuilderSlot.getClusterNodeMap().clear();
        AdaptiveDegradeRuleManager.getRule(RESOURCE_NAME).setEnabled(true);
    }

    @After
    public void tearDown() {
        AdaptiveDegradeRuleManager.getRule(RESOURCE_NAME).setEnabled(false);
        ClusterBuilderSlot.getClusterNodeMap().clear();
        if (client != null) {
            try {
                client.shutdown();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
        server.stop();
    }

    @Test
    public void testAdaptiveHeadersAndMetrics() throws Exception {
        int port = 19330;
        server.start(port, true); // enable server interceptor
        client = new FooServiceClient("localhost", port, new SentinelGrpcClientInterceptor());
        FooRequest request = FooRequest.newBuilder().setName("AdaptiveTest").setId(123).build();
        FooResponse response = client.sayHello(request);
        assertNotNull(response);
        assertTrue(response.getMessage().contains("AdaptiveTest"));
        ClusterNode node = ClusterBuilderSlot.getClusterNode(RESOURCE_NAME, EntryType.OUT);
        assertNotNull(node);
        assertTrue(node.totalPass() >= 1);
        AdaptiveServerMetric metric = AdaptiveDegradeRuleManager.getServerMetric(RESOURCE_NAME);
        assertNotNull(metric);
    }

    @Test
    public void testAdaptiveHeaderNotSentWhenDisabled() throws Exception {
        AdaptiveDegradeRuleManager.getRule(RESOURCE_NAME).setEnabled(false);
        AtomicReference<String> adaptiveHeaderValue = new AtomicReference<>();
        ServerInterceptor capturingInterceptor = new ServerInterceptor() {
            @Override
            public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(
                    ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
                Metadata.Key<String> key = Metadata.Key.of("X-Sentinel-Adaptive", Metadata.ASCII_STRING_MARSHALLER);
                adaptiveHeaderValue.set(headers.get(key));
                return next.startCall(call, headers);
            }
        };
        Server serverInstance = ServerBuilder.forPort(0)
                .addService(new FooServiceImpl())
                .intercept(capturingInterceptor)
                .intercept(new SentinelGrpcServerInterceptor())
                .build();
        serverInstance.start();
        try {
            int port = serverInstance.getPort();
            client = new FooServiceClient("localhost", port, new SentinelGrpcClientInterceptor());
            FooRequest request = FooRequest.newBuilder().setName("NoAdaptive").setId(456).build();
            client.sayHello(request);
            assertNull(adaptiveHeaderValue.get());
        } finally {
            if (client != null) {
                client.shutdown();
            }
            serverInstance.shutdownNow();
            serverInstance.awaitTermination(5, TimeUnit.SECONDS);
        }
    }
}