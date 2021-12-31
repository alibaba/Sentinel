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
package com.alibaba.csp.sentinel.adapter.servletmethod;

import com.alibaba.csp.sentinel.adapter.servlet.config.WebServletConfig;
import com.alibaba.csp.sentinel.adapter.servlet.util.FilterUtil;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author zhaoyuguang
 * @author Roger Law
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class CommonFilterMethodTest {

    private static final String HELLO_STR = "Hello!";

    private static final String HELLO_POST_STR = "Hello Post!";

    private static final String GET = "GET";

    private static final String POST = "POST";

    private static final String COLON = ":";

    @Autowired
    private MockMvc mvc;

    private void configureRulesFor(String resource, int count) {
        configureRulesFor(resource, count, "default");
    }

    private void configureRulesFor(String resource, int count, String limitApp) {
        FlowRule rule = new FlowRule()
                .setCount(count)
                .setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setResource(resource);
        if (StringUtil.isNotBlank(limitApp)) {
            rule.setLimitApp(limitApp);
        }
        FlowRuleManager.loadRules(Collections.singletonList(rule));
    }

    @Test
    public void testCommonFilterMiscellaneous() throws Exception {
        String url = "/hello";
        this.mvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(content().string(HELLO_STR));

        ClusterNode cnGet = ClusterBuilderSlot.getClusterNode(GET + COLON + url);
        assertNotNull(cnGet);
        assertEquals(1, cnGet.passQps(), 0.01);


        ClusterNode cnPost = ClusterBuilderSlot.getClusterNode(POST + COLON + url);
        assertNull(cnPost);

        this.mvc.perform(post(url))
                .andExpect(status().isOk())
                .andExpect(content().string(HELLO_POST_STR));

        cnPost = ClusterBuilderSlot.getClusterNode(POST + COLON + url);
        assertNotNull(cnPost);
        assertEquals(1, cnPost.passQps(), 0.01);

        testCommonBlockAndRedirectBlockPage(url, cnGet, cnPost);
    }

    private void testCommonBlockAndRedirectBlockPage(String url, ClusterNode cnGet, ClusterNode cnPost) throws Exception {
        configureRulesFor(GET + ":" + url, 0);
        // The request will be blocked and response is default block message.
        this.mvc.perform(get(url).accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isTooManyRequests())
                .andExpect(content().string(FilterUtil.DEFAULT_BLOCK_MSG));
        assertEquals(1, cnGet.blockQps(), 0.01);

        // Test for post pass
        this.mvc.perform(post(url))
                .andExpect(status().isOk())
                .andExpect(content().string(HELLO_POST_STR));

        assertEquals(2, cnPost.passQps(), 0.01);


        FlowRuleManager.loadRules(null);
        WebServletConfig.setBlockPage("");
    }

    @After
    public void cleanUp() {
        FlowRuleManager.loadRules(null);
        ClusterBuilderSlot.resetClusterNodes();
    }
}
