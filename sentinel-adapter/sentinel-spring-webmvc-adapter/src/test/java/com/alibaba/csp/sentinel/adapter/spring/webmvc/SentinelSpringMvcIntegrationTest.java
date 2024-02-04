/*
 * Copyright 1999-2019 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.adapter.spring.webmvc;

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
import org.springframework.test.web.servlet.MvcResult;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author kaizi2009
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
public class SentinelSpringMvcIntegrationTest {

    private static final String HELLO_STR = "Hello!";
    @Autowired
    private MockMvc mvc;

    // async request  test
    @Test
    public void testAsync() throws Exception {
        String url = "/async";
        MvcResult mvcResult = this.mvc.perform(get(url))
                .andExpect(request().asyncStarted())
                .andReturn();
        this.mvc.perform(asyncDispatch(mvcResult))
                .andExpect(status().isOk())
                .andExpect(content().string("apiAsync!"));

        ClusterNode cn = ClusterBuilderSlot.getClusterNode(url);
        assertNotNull(cn);
        assertEquals(1, cn.successQps(), 0.01);
    }

    @Test
    public void testNestedAsync() throws Exception {
        String url = "/nestedAsync";
        MvcResult mvcResult = this.mvc.perform(get(url))
                .andExpect(request().asyncStarted())
                .andReturn();
        MvcResult mvcResult2 = this.mvc.perform(asyncDispatch(mvcResult))
                .andExpect(forwardedUrl("async"))
                .andExpect(status().isOk())
                .andReturn();

        //Mock mvc does not support automatic jump forward requests.
        //If manual requests are made in the following ways, the count will be re-counted
        assertNotNull(mvcResult2.getModelAndView());
        assertNotNull(mvcResult2.getModelAndView().getViewName());
        MvcResult mvcResult3 = this.mvc.perform(get("/" + mvcResult2.getModelAndView().getViewName()))
                .andExpect(request().asyncStarted())
                .andReturn();
        this.mvc.perform(asyncDispatch(mvcResult3))
                .andExpect(status().isOk())
                .andExpect(content().string("apiAsync!"));
        ClusterNode cn = ClusterBuilderSlot.getClusterNode(url);
        assertNotNull(cn);
        assertEquals(1, cn.successQps(), 0.01);
    }

    @Test
    public void testAsync2Sync() throws Exception {
        String url = "/async2Sync";
        MvcResult mvcResult = this.mvc.perform(get(url))
                .andExpect(request().asyncStarted())
                .andReturn();
        MvcResult mvcResult2 = this.mvc.perform(asyncDispatch(mvcResult))
                .andExpect(forwardedUrl("sync"))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(mvcResult2.getModelAndView());
        assertNotNull(mvcResult2.getModelAndView().getViewName());
        this.mvc.perform(get("/" + mvcResult2.getModelAndView().getViewName()))
                .andExpect(status().isOk())
                .andExpect(content().string("sync!"));

        ClusterNode cn = ClusterBuilderSlot.getClusterNode(url);
        assertNotNull(cn);
        assertEquals(1, cn.successQps(), 0.01);
    }

    @Test
    public void testAsync2Sync2Async() throws Exception {
        String url = "/async2Sync2Async";
        MvcResult mvcResult = this.mvc.perform(get(url))
                .andExpect(request().asyncStarted())
                .andReturn();
        MvcResult mvcResult2 = this.mvc.perform(asyncDispatch(mvcResult))
                .andExpect(forwardedUrl("sync2Async"))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(mvcResult2.getModelAndView());
        assertNotNull(mvcResult2.getModelAndView().getViewName());
        MvcResult mvcResult3 = this.mvc.perform(get("/" + mvcResult2.getModelAndView().getViewName()))
                .andExpect(forwardedUrl("async"))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(mvcResult3.getModelAndView());
        assertNotNull(mvcResult3.getModelAndView().getViewName());
        MvcResult mvcResult4 = this.mvc.perform(get("/" + mvcResult3.getModelAndView().getViewName()))
                .andExpect(request().asyncStarted())
                .andReturn();
        this.mvc.perform(asyncDispatch(mvcResult4))
                .andExpect(status().isOk())
                .andExpect(content().string("apiAsync!"));

        ClusterNode cn = ClusterBuilderSlot.getClusterNode(url);
        assertNotNull(cn);
        assertEquals(1, cn.successQps(), 0.01);
    }


    @Test
    public void testSync2Async() throws Exception {
        String url = "/sync2Async";
        MvcResult mvcResult = this.mvc.perform(get(url))
                .andExpect(forwardedUrl("async"))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(mvcResult.getModelAndView());
        assertNotNull(mvcResult.getModelAndView().getViewName());
        MvcResult mvcResult2 = this.mvc.perform(get("/" + mvcResult.getModelAndView().getViewName()))
                .andExpect(request().asyncStarted())
                .andReturn();
        this.mvc.perform(asyncDispatch(mvcResult2))
                .andExpect(status().isOk())
                .andExpect(content().string("apiAsync!"));
        ClusterNode cn = ClusterBuilderSlot.getClusterNode(url);
        assertNotNull(cn);
        assertEquals(1, cn.successQps(), 0.01);
    }


    @Test
    public void testSync2Exception() throws Exception {
        String url = "/sync2Exception";
        MvcResult mvcResult = this.mvc.perform(get(url))
                .andExpect(forwardedUrl("runtimeException"))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(mvcResult.getModelAndView());
        assertNotNull(mvcResult.getModelAndView().getViewName());
        this.mvc.perform(get("/" + mvcResult.getModelAndView().getViewName()))
                .andExpect(status().isOk())
                .andExpect(content().string(ResultWrapper.error().toJsonString()));

        ClusterNode cn = ClusterBuilderSlot.getClusterNode(url);
        assertNotNull(cn);
        assertEquals(1, cn.successQps(), 0.01);
    }

    @Test
    public void testAsync2Exception() throws Exception {
        String url = "/async2Exception";
        MvcResult mvcResult = this.mvc.perform(get(url))
                .andExpect(request().asyncStarted())
                .andReturn();
        MvcResult mvcResult2 = this.mvc.perform(asyncDispatch(mvcResult))
                .andExpect(forwardedUrl("runtimeException"))
                .andExpect(status().isOk())
                .andReturn();
        assertNotNull(mvcResult2.getModelAndView());
        assertNotNull(mvcResult2.getModelAndView().getViewName());
        this.mvc.perform(get("/" + mvcResult2.getModelAndView().getViewName()))
                .andExpect(status().isOk())
                .andExpect(content().string(ResultWrapper.error().toJsonString()));

        ClusterNode cn = ClusterBuilderSlot.getClusterNode(url);
        assertNotNull(cn);
        assertEquals(1, cn.successQps(), 0.01);
    }


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
    public void testOriginParser() throws Exception {
        String springMvcPathVariableUrl = "/foo/{id}";
        String limitOrigin = "userA";
        final String headerName = "S-User";
        configureRulesFor(springMvcPathVariableUrl, 0, limitOrigin);

        // This will be passed since the caller is different: userB
        this.mvc.perform(get("/foo/1").accept(MediaType.TEXT_PLAIN).header(headerName, "userB"))
                .andExpect(status().isOk())
                .andExpect(content().string("foo 1"));

        // This will be blocked since the caller is same: userA
        this.mvc.perform(
                get("/foo/2").accept(MediaType.APPLICATION_JSON).header(headerName, limitOrigin))
                .andExpect(status().isOk())
                .andExpect(content().json(ResultWrapper.blocked().toJsonString()));

        // This will be passed since the caller is different: ""
        this.mvc.perform(get("/foo/3").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("foo 3"));

        FlowRuleManager.loadRules(null);
    }

    @Test
    public void testTotalInterceptor() throws Exception {
        String url = "/hello";
        String totalTarget = "my_spring_mvc_total_url_request";
        for (int i = 0; i < 3; i++) {
            this.mvc.perform(get(url))
                    .andExpect(status().isOk())
                    .andExpect(content().string(HELLO_STR));
        }
        ClusterNode cn = ClusterBuilderSlot.getClusterNode(totalTarget);
        assertNotNull(cn);
        assertEquals(3, cn.passQps(), 0.01);
    }

    @Test
    public void testRuntimeException() throws Exception {
        String url = "/runtimeException";
        configureExceptionRulesFor(url, 3, null);
        int repeat = 3;
        for (int i = 0; i < repeat; i++) {
            this.mvc.perform(get(url))
                    .andExpect(status().isOk())
                    .andExpect(content().string(ResultWrapper.error().toJsonString()));
            ClusterNode cn = ClusterBuilderSlot.getClusterNode(url);
            assertNotNull(cn);
            assertEquals(i + 1, cn.passQps(), 0.01);
        }

        // This will be blocked and response json.
        this.mvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(content().string(ResultWrapper.blocked().toJsonString()));
        ClusterNode cn = ClusterBuilderSlot.getClusterNode(url);
        assertNotNull(cn);
        assertEquals(repeat, cn.passQps(), 0.01);
        assertEquals(1, cn.blockRequest(), 1);
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

    private void configureExceptionRulesFor(String resource, int count, String limitApp) {
        FlowRule rule = new FlowRule()
                .setCount(count)
                .setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
        rule.setResource(resource);
        if (StringUtil.isNotBlank(limitApp)) {
            rule.setLimitApp(limitApp);
        }
        FlowRuleManager.loadRules(Collections.singletonList(rule));
    }

    @After
    public void cleanUp() {
        FlowRuleManager.loadRules(null);
        ClusterBuilderSlot.resetClusterNodes();
    }
}
