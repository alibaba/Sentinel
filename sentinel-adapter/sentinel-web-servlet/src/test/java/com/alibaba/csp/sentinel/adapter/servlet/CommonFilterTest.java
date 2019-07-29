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
package com.alibaba.csp.sentinel.adapter.servlet;

import java.util.Collections;

import javax.servlet.http.HttpServletRequest;

import com.alibaba.csp.sentinel.adapter.servlet.callback.DefaultUrlCleaner;
import com.alibaba.csp.sentinel.adapter.servlet.callback.RequestOriginParser;
import com.alibaba.csp.sentinel.adapter.servlet.callback.UrlCleaner;
import com.alibaba.csp.sentinel.adapter.servlet.callback.WebCallbackManager;
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

import static org.junit.Assert.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * @author Eric Zhao
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
public class CommonFilterTest {

    private static final String HELLO_STR = "Hello!";

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

        ClusterNode cn = ClusterBuilderSlot.getClusterNode(url);
        assertNotNull(cn);
        assertEquals(1, cn.passQps(), 0.01);

        testCommonBlockAndRedirectBlockPage(url, cn);

        // Test for url cleaner.
        testUrlCleaner();
        testUrlExclusion();
        testCustomOriginParser();
    }

    private void testCommonBlockAndRedirectBlockPage(String url, ClusterNode cn) throws Exception {
        configureRulesFor(url, 0);
        // The request will be blocked and response is default block message.
        this.mvc.perform(get(url).accept(MediaType.TEXT_PLAIN))
            .andExpect(status().isOk())
            .andExpect(content().string(FilterUtil.DEFAULT_BLOCK_MSG));
        assertEquals(1, cn.blockQps(), 0.01);

        // Test for redirect.
        String redirectUrl = "http://some-location.com";
        WebServletConfig.setBlockPage(redirectUrl);
        this.mvc.perform(get(url).accept(MediaType.TEXT_PLAIN))
            .andExpect(status().is3xxRedirection())
            .andExpect(header().string("Location", redirectUrl + "?http_referer=http://localhost/hello"));

        FlowRuleManager.loadRules(null);
        WebServletConfig.setBlockPage("");
    }

    private void testUrlCleaner() throws Exception {
        final String fooPrefix = "/foo/";
        String url1 = fooPrefix + 1;
        String url2 = fooPrefix + 2;
        WebCallbackManager.setUrlCleaner(new UrlCleaner() {
            @Override
            public String clean(String originUrl) {
                if (originUrl.startsWith(fooPrefix)) {
                    return "/foo/*";
                }
                return originUrl;
            }
        });
        this.mvc.perform(get(url1).accept(MediaType.TEXT_PLAIN))
            .andExpect(status().isOk())
            .andExpect(content().string("Hello 1"));
        this.mvc.perform(get(url2).accept(MediaType.TEXT_PLAIN))
            .andExpect(status().isOk())
            .andExpect(content().string("Hello 2"));
        ClusterNode cn = ClusterBuilderSlot.getClusterNode(fooPrefix + "*");
        assertEquals(2, cn.passQps(), 0.01);
        assertNull(ClusterBuilderSlot.getClusterNode(url1));
        assertNull(ClusterBuilderSlot.getClusterNode(url2));

        WebCallbackManager.setUrlCleaner(new DefaultUrlCleaner());
    }

    private void testUrlExclusion() throws Exception {
        final String excludePrefix = "/exclude/";
        String url = excludePrefix + 1;
        WebCallbackManager.setUrlCleaner(new UrlCleaner() {
            @Override
            public String clean(String originUrl) {
                if(originUrl.startsWith(excludePrefix)) {
                    return "";
                }
                return originUrl;
            }
        });
        this.mvc.perform(get(url).accept(MediaType.TEXT_PLAIN))
                .andExpect(status().isOk())
                .andExpect(content().string("Exclude 1"));
        assertNull(ClusterBuilderSlot.getClusterNode(url));
        WebCallbackManager.setUrlCleaner(new DefaultUrlCleaner());
    }

    private void testCustomOriginParser() throws Exception {
        String url = "/hello";
        String limitOrigin = "userA";
        final String headerName = "S-User";
        configureRulesFor(url, 0, limitOrigin);

        WebCallbackManager.setRequestOriginParser(new RequestOriginParser() {
            @Override
            public String parseOrigin(HttpServletRequest request) {
                String origin = request.getHeader(headerName);
                return origin != null ? origin : "";
            }
        });

        this.mvc.perform(get(url).accept(MediaType.TEXT_PLAIN).header(headerName, "userB"))
            .andExpect(status().isOk())
            .andExpect(content().string(HELLO_STR));
        // This will be blocked.
        this.mvc.perform(get(url).accept(MediaType.TEXT_PLAIN).header(headerName, limitOrigin))
            .andExpect(status().isOk())
            .andExpect(content().string(FilterUtil.DEFAULT_BLOCK_MSG));
        this.mvc.perform(get(url).accept(MediaType.TEXT_PLAIN))
            .andExpect(status().isOk())
            .andExpect(content().string(HELLO_STR));

        WebCallbackManager.setRequestOriginParser(null);
        FlowRuleManager.loadRules(null);
    }

    @After
    public void cleanUp() {
        FlowRuleManager.loadRules(null);
        ClusterBuilderSlot.resetClusterNodes();
    }
}
