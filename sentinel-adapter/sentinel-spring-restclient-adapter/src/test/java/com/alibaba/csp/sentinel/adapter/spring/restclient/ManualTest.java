package com.alibaba.csp.sentinel.adapter.spring.restclient;

import com.alibaba.csp.sentinel.adapter.spring.restclient.extractor.RestClientResourceExtractor;
import com.alibaba.csp.sentinel.adapter.spring.restclient.fallback.RestClientFallback;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;

import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.web.client.RestClient;

import com.alibaba.csp.sentinel.slots.block.BlockException;

import java.util.Collections;

/**
 * Manual test for RestClient adapter.
 * 
 * Run this class directly to test the adapter functionality.
 *
 * @author uuuyuqi
 */
public class ManualTest {

    public static void main(String[] args) {
        System.out.println("=== Sentinel RestClient Adapter Manual Test ===\n");

        testBasicUsage();
        testWithFlowControl();
        testWithCustomExtractor();
        
        System.out.println("\n=== All manual tests completed! ===");
    }

    private static void testBasicUsage() {
        System.out.println("Test 1: Basic Usage");
        System.out.println("--------------------");
        
        RestClient restClient = RestClient.builder()
                .requestInterceptor(new SentinelRestClientInterceptor())
                .build();

        try {
            String result = restClient.get()
                    .uri("https://httpbin.org/get")
                    .retrieve()
                    .body(String.class);
            
            System.out.println("✅ Request successful!");
            System.out.println("Response length: " + (result != null ? result.length() : 0) + " chars");
            
            ClusterNode node = ClusterBuilderSlot.getClusterNode("restclient:GET:https://httpbin.org/get");
            if (node != null) {
                System.out.println("✅ Sentinel statistics recorded!");
                System.out.println("   Total requests: " + node.totalRequest());
                System.out.println("   Success: " + node.totalSuccess());
            }
        } catch (Exception e) {
            System.out.println("❌ Test failed: " + e.getMessage());
        }
        
        System.out.println();
    }

    private static void testWithFlowControl() {
        System.out.println("Test 2: Flow Control");
        System.out.println("---------------------");
        
        String resourceName = "restclient:GET:https://httpbin.org/delay/1";
        
        FlowRule rule = new FlowRule(resourceName);
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setCount(0);
        rule.setLimitApp("default");
        FlowRuleManager.loadRules(Collections.singletonList(rule));

        RestClient restClient = RestClient.builder()
                .requestInterceptor(new SentinelRestClientInterceptor())
                .build();

        try {
            restClient.get()
                    .uri("https://httpbin.org/delay/1")
                    .retrieve()
                    .body(String.class);
            System.out.println("❌ Request should have been blocked!");
        } catch (com.alibaba.csp.sentinel.slots.block.SentinelRpcException e) {
            System.out.println("✅ Request blocked as expected: " + e.getCause().getClass().getSimpleName());
        } catch (Exception e) {
            System.out.println("❌ Unexpected exception: " + e.getMessage());
        }
        
        FlowRuleManager.loadRules(Collections.emptyList());
        System.out.println();
    }

    private static void testWithCustomExtractor() {
        System.out.println("Test 3: Custom Resource Extractor");
        System.out.println("-----------------------------------");
        
        RestClientResourceExtractor customExtractor = request -> {
            String path = request.getURI().getPath();
            if (path.matches("/status/\\d+")) {
                path = "/status/{code}";
            }
            return request.getMethod().toString() + ":" + 
                   request.getURI().getHost() + path;
        };

        RestClientFallback customFallback = (HttpRequest request, byte[] body,
                                             ClientHttpRequestExecution execution, BlockException ex) -> {
            throw new RuntimeException("Custom fallback: " + ex.getClass().getSimpleName(), ex);
        };

        SentinelRestClientConfig config = new SentinelRestClientConfig(
                "custom:",
                customExtractor,
                customFallback
        );

        RestClient restClient = RestClient.builder()
                .requestInterceptor(new SentinelRestClientInterceptor(config))
                .build();

        try {
            String result = restClient.get()
                    .uri("https://httpbin.org/status/200")
                    .retrieve()
                    .body(String.class);
            
            System.out.println("Response: " + (result != null ? "OK" : "Empty"));
            System.out.println("✅ Custom extractor test completed!");
            
            String expectedResource = "custom:GET:httpbin.org/status/{code}";
            ClusterNode node = ClusterBuilderSlot.getClusterNode(expectedResource);
            if (node != null) {
                System.out.println("✅ Resource name normalized correctly!");
                System.out.println("   Resource: " + expectedResource);
            }
        } catch (Exception e) {
            System.out.println("Response: " + e.getMessage());
        }
        
    }
}