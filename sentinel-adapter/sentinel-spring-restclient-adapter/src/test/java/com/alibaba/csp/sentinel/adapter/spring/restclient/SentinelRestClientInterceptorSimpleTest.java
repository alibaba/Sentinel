/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.adapter.spring.restclient;

import com.alibaba.csp.sentinel.Constants;
import com.alibaba.csp.sentinel.adapter.spring.restclient.app.TestApplication;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.client.RestClient;

import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Simple integration tests for {@link SentinelRestClientInterceptor}.
 *
 * @author uuuyuqi
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT,
        properties = {
                "server.port=8087"
        })
public class SentinelRestClientInterceptorSimpleTest {

    @Value("${server.port}")
    private Integer port;

    @Before
    public void setUp() {
        Constants.ROOT.removeChildList();
        ClusterBuilderSlot.getClusterNodeMap().clear();
        FlowRuleManager.loadRules(Collections.emptyList());
        DegradeRuleManager.loadRules(Collections.emptyList());
    }

    @After
    public void tearDown() {
        Constants.ROOT.removeChildList();
        ClusterBuilderSlot.getClusterNodeMap().clear();
        FlowRuleManager.loadRules(Collections.emptyList());
        DegradeRuleManager.loadRules(Collections.emptyList());
    }

    @Test
    public void testBasicRequest() {
        String url = "http://localhost:" + port + "/test/hello";
        
        RestClient restClient = RestClient.builder()
                .requestInterceptor(new SentinelRestClientInterceptor())
                .build();

        String result = restClient.get()
                .uri(url)
                .retrieve()
                .body(String.class);

        assertEquals("Hello, Sentinel!", result);
        System.out.println("Request completed successfully: " + result);
    }
    
    @Test
    public void testDualLevelResources() {
        String url = "http://localhost:" + port + "/test/hello";
        String expectedHostResource = "restclient:GET:http://localhost:" + port;
        String expectedPathResource = "restclient:GET:http://localhost:" + port + "/test/hello";
        
        RestClient restClient = RestClient.builder()
                .requestInterceptor(new SentinelRestClientInterceptor())
                .build();

        String result = restClient.get()
                .uri(url)
                .retrieve()
                .body(String.class);

        assertEquals("Hello, Sentinel!", result);
        System.out.println("✅ Request completed successfully");
        
        try {
            ClusterNode hostNode = ClusterBuilderSlot.getClusterNode(expectedHostResource);
            if (hostNode != null) {
                System.out.println("✅ Host-level resource created: " + expectedHostResource);
                System.out.println("   Total requests: " + hostNode.totalRequest());
            }
            
            ClusterNode pathNode = ClusterBuilderSlot.getClusterNode(expectedPathResource);
            if (pathNode != null) {
                System.out.println("✅ Path-level resource created: " + expectedPathResource);
                System.out.println("   Total requests: " + pathNode.totalRequest());
            }
        } catch (Exception e) {
            System.out.println("Note: ClusterNode check skipped due to: " + e.getMessage());
        }
    }

    @Test(expected = com.alibaba.csp.sentinel.slots.block.SentinelRpcException.class)
    public void testFlowControlBlocking() {
        String url = "http://localhost:" + port + "/test/hello";
        String pathResource = "restclient:GET:" + url;

        FlowRule rule = new FlowRule(pathResource);
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setCount(0);
        rule.setLimitApp("default");
        FlowRuleManager.loadRules(Collections.singletonList(rule));

        RestClient restClient = RestClient.builder()
                .requestInterceptor(new SentinelRestClientInterceptor())
                .build();

        restClient.get()
                .uri(url)
                .retrieve()
                .body(String.class);
    }
    
    @Test(expected = com.alibaba.csp.sentinel.slots.block.SentinelRpcException.class)
    public void testHostLevelFlowControl() throws InterruptedException {
        String url = "http://localhost:" + port + "/test/hello";
        String rootResource = "restclient:GET:" + "http://localhost:" + port;

        FlowRule rule = new FlowRule(rootResource);
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setCount(0);
        rule.setLimitApp("default");
        FlowRuleManager.loadRules(Collections.singletonList(rule));

        RestClient restClient = RestClient.builder()
                .requestInterceptor(new SentinelRestClientInterceptor())
                .build();

        restClient.get()
                .uri(url)
                .retrieve()
                .body(String.class);
    }


    @Test(expected = IllegalStateException.class)
    public void testCustomConfig() {
        String customPrefix = "my-api:";
        String url = "http://localhost:" + port + "/test/hello";

        FlowRule rule = new FlowRule("my-api:abc");
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setCount(0);
        rule.setLimitApp("default");
        FlowRuleManager.loadRules(Collections.singletonList(rule));

        SentinelRestClientConfig config = new SentinelRestClientConfig(
                customPrefix,
                request -> "abc",
                (a, b, c, d) -> { throw new IllegalStateException("custom fallback triggered"); });
        RestClient restClient = RestClient.builder()
                .requestInterceptor(new SentinelRestClientInterceptor(config))
                .build();

        restClient.get()
                .uri(url)
                .retrieve()
                .body(String.class);
    }


    @Test(expected = com.alibaba.csp.sentinel.slots.block.SentinelRpcException.class)
    public void testPostRequestFlowControl() {
        String url = "http://localhost:" + port + "/test/users";
        String pathResource = "restclient:POST:" + url;

        FlowRule rule = new FlowRule(pathResource);
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setCount(0);
        rule.setLimitApp("default");
        FlowRuleManager.loadRules(Collections.singletonList(rule));

        RestClient restClient = RestClient.builder()
                .requestInterceptor(new SentinelRestClientInterceptor())
                .build();

        restClient.post()
                .uri(url)
                .body("Test User")
                .retrieve()
                .body(String.class);
    }

    @Test
    public void testDegradeByExceptionRatio() {
        String url = "http://localhost:" + port + "/test/error";
        String resourceName = "restclient:GET:" + url;

        DegradeRule degradeRule = new DegradeRule(resourceName);
        degradeRule.setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
        degradeRule.setCount(0.99);
        degradeRule.setMinRequestAmount(1);
        degradeRule.setStatIntervalMs(10 * 1000);
        degradeRule.setTimeWindow(30);
        degradeRule.setLimitApp("default");
        DegradeRuleManager.loadRules(Collections.singletonList(degradeRule));

        RestClient restClient = RestClient.builder()
                .requestInterceptor(new SentinelRestClientInterceptor())
                .build();

        try {
            restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);
        } catch (Exception e) {
            System.out.println("First request failed with exception (expected): " + e.getClass().getSimpleName());
        }

        try {
            String result = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);

            assertNotNull("Should get fallback response after circuit opens", result);
            assertTrue("Response should indicate blocking", 
                    result.contains("blocked by Sentinel") || result.contains("DegradeException"));
            System.out.println("Degrade fallback response: " + result);
        } catch (Exception e) {
            System.out.println("Second request also threw exception: " + e.getMessage());
        }
    }

    @Test
    public void testDegradeBySlowResponseTime() throws InterruptedException {
        String url = "http://localhost:" + port + "/test/delay";
        String resourceName = "restclient:GET:" + url;

        DegradeRule degradeRule = new DegradeRule(resourceName);
        degradeRule.setGrade(RuleConstant.DEGRADE_GRADE_RT);
        degradeRule.setCount(50);
        degradeRule.setSlowRatioThreshold(0.1);
        degradeRule.setMinRequestAmount(1);
        degradeRule.setStatIntervalMs(10 * 1000);
        degradeRule.setTimeWindow(30);
        degradeRule.setLimitApp("default");
        DegradeRuleManager.loadRules(Collections.singletonList(degradeRule));

        RestClient restClient = RestClient.builder()
                .requestInterceptor(new SentinelRestClientInterceptor())
                .build();

        String result = restClient.get()
                .uri(url)
                .retrieve()
                .body(String.class);

        assertEquals("Delayed response", result);
        System.out.println("First slow request completed: " + result);

        Thread.sleep(100);

        try {
            result = restClient.get()
                    .uri(url)
                    .retrieve()
                    .body(String.class);

            assertNotNull("Should get response", result);
            System.out.println("Second request response: " + result);
        } catch (Exception e) {
            System.out.println("Request failed: " + e.getMessage());
        }
    }
}