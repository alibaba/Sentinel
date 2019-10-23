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
package com.alibaba.csp.sentinel.adapter.spring.webmvc;

import java.util.Collections;

import com.alibaba.csp.sentinel.adapter.spring.webmvc.test.SpringWebmvcTestApplication;
import com.alibaba.csp.sentinel.adapter.spring.webmvc.test.SpringWebmvcTestConfig;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;
import com.alibaba.scp.sentinel.adapter.spring.webmvc.util.InterceptorUtil;

import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author zhaoyuguang
 */

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringWebmvcTestApplication.class)
@AutoConfigureMockMvc
public class SentinelSpringWebmvcIntegrationTest {

    private static final String HELLO_STR = "Hello!";
    @Autowired
    private MockMvc mvc;

    @Test
    public void testBase() throws Exception {
        String url = "/hello";
        this.mvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(content().string(HELLO_STR));
        ClusterNode cn = ClusterBuilderSlot.getClusterNode(url);
        assertNotNull(cn);
        assertEquals(1, cn.passQps(), 0.01);
    }

    @Test
    public void testInterceptor() throws Exception {
        for (int i = 0; i < 3; i++) {
            this.mvc.perform(get("/foo/foo"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("get:foo"));
        }
        ClusterNode cn = ClusterBuilderSlot.getClusterNode("/foo/{id}");
        assertNotNull(cn);
        assertEquals(3, cn.passQps(), 0.01);
    }

    @Test
    public void testUrlCleaner() throws Exception {
        for (int i = 0; i < 3; i++) {
            this.mvc.perform(get("/foo/clean"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("clean"));
        }
        ClusterNode cn = ClusterBuilderSlot.getClusterNode("/foo/clean");
        assertNull(cn);
    }

    @Test
    public void testCustomOriginParser() throws Exception {
        String url = "/foo/foo";
        String limitOrigin = "userA";

        configureRulesFor("/foo/{id}", 0, limitOrigin);

        this.mvc.perform(get(url).accept(MediaType.TEXT_PLAIN).header(SpringWebmvcTestConfig.HEADER_NAME, "userB"))
                .andExpect(status().isOk())
                .andExpect(content().string("get:foo"));
        this.mvc.perform(get(url).accept(MediaType.TEXT_PLAIN).header(SpringWebmvcTestConfig.HEADER_NAME, limitOrigin))
                .andExpect(status().isOk())
                .andExpect(content().string(InterceptorUtil.DEFAULT_BLOCK_MSG));
    }

    private void configureRulesFor(String resource, int count, String limitApp) {
        FlowRule rule = new FlowRule()
                .setCount(count)
                .setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setResource(resource);
        rule.setLimitApp(limitApp);
        FlowRuleManager.loadRules(Collections.singletonList(rule));
    }

    @After
    public void cleanUp() {
        FlowRuleManager.loadRules(null);
        ClusterBuilderSlot.resetClusterNodes();
    }
}
