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
package com.alibaba.csp.sentinel.dashboard.client;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.annotation.aspectj.SentinelResourceAspect;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.apache.http.HttpException;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.protocol.RequestContent;
import org.junit.Test;
import org.springframework.context.annotation.Bean;

public class SentinelApiClientTest {
    @Test
    public void postRequest() throws HttpException, IOException {
        // Processor is required because it will determine the final request body including
        // headers before outgoing.
        RequestContent processor = new RequestContent();
        Map<String, String> params = new HashMap<String, String>();
        params.put("a", "1");
        params.put("b", "2+");
        params.put("c", "3 ");
        
        HttpUriRequest request;
        
        request = SentinelApiClient.postRequest("/test", params, false);
        assertNotNull(request);
        processor.process(request, null);
        assertNotNull(request.getFirstHeader("Content-Type"));
        assertEquals("application/x-www-form-urlencoded", request.getFirstHeader("Content-Type").getValue());
        
        request = SentinelApiClient.postRequest("/test", params, true);
        assertNotNull(request);
        processor.process(request, null);
        assertNotNull(request.getFirstHeader("Content-Type"));
        assertEquals("application/x-www-form-urlencoded; charset=UTF-8", request.getFirstHeader("Content-Type").getValue());
    }
    @Bean
    public SentinelResourceAspect sentinelResourceAspect() {
        return new SentinelResourceAspect();
    }
    public static void main(String[] args) {
        initFlowRules();
        while (true) {
            Entry entry = null;
            try {
                entry = SphU.entry("HelloWorld-resource");
                System.out.println("hello world");
            } catch (BlockException e1) {
                System.out.println("block!");
            } finally {
                if (entry != null) {
                    entry.exit();
                }
            }
        }
    }
    private static void initFlowRules(){
        List<FlowRule> rules = new ArrayList<>();
        FlowRule rule = new FlowRule();
        rule.setResource("HelloWorld-resource");
        rule.setGrade(RuleConstant.FLOW_GRADE_QPS);
        // Set limit QPS
        rule.setCount(10);
        rules.add(rule);
        FlowRuleManager.loadRules(rules);
    }

}
