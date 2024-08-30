package com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x;

import com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.config.DefaultInterceptorConfig;
import com.alibaba.csp.sentinel.adapter.spring.webmvc_v6x.config.InterceptorConfig;
import com.alibaba.csp.sentinel.adapter.web.common.DefaultBlockExceptionResponse;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeException;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowException;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author Lingzhi
 */
@RunWith(SpringRunner.class)
@Import(DefaultInterceptorConfig.class)
@WebMvcTest(excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = InterceptorConfig.class))
public class SentinelDefaultBlockExceptionHandlerTest {
    @Autowired
    private MockMvc mvc;

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
        DefaultBlockExceptionResponse res = DefaultBlockExceptionResponse.FLOW_EXCEPTION;
        this.mvc.perform(
                        get("/foo/2").accept(MediaType.APPLICATION_JSON).header(headerName, limitOrigin))
                .andExpect(status().is(res.getStatus()))
                .andExpect(content().string(res.getMsg()));

        // This will be passed since the caller is different: ""
        this.mvc.perform(get("/foo/3").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string("foo 3"));

        FlowRuleManager.loadRules(null);
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
        DefaultBlockExceptionResponse res = DefaultBlockExceptionResponse.resolve(FlowException.class);
        this.mvc.perform(get(url))
                .andExpect(status().is(res.getStatus()))
                .andExpect(content().string(res.getMsg()));
        ClusterNode cn = ClusterBuilderSlot.getClusterNode(url);
        assertNotNull(cn);
        assertEquals(repeat, cn.passQps(), 0.01);
        assertEquals(1, cn.blockRequest(), 1);

        FlowRuleManager.loadRules(null);
    }


    @Test
    public void testExceptionPerception() throws Exception {
        String url = "/bizException";
        configureExceptionDegradeRulesFor(url, 2.6, null);
        int repeat = 3;
        for (int i = 0; i < repeat; i++) {
            this.mvc.perform(get(url))
                    .andExpect(status().isOk())
                    .andExpect(content().string(new ResultWrapper(-1, "Biz error").toJsonString()));

            ClusterNode cn = ClusterBuilderSlot.getClusterNode(url);
            assertNotNull(cn);
            assertEquals(i + 1, cn.passQps(), 0.01);
        }

        // This will be blocked and response.
        DefaultBlockExceptionResponse res = DefaultBlockExceptionResponse.resolve(DegradeException.class);
        this.mvc.perform(get(url))
                .andExpect(status().is(res.getStatus()))
                .andExpect(content().string(res.getMsg()));
        ClusterNode cn = ClusterBuilderSlot.getClusterNode(url);
        assertNotNull(cn);
        assertEquals(repeat, cn.passQps(), 0.01);
        assertEquals(1, cn.blockRequest(), 1);
    }

    private void configureRulesFor(String resource, int count, String limitApp) {
        FlowRule rule = new FlowRule().setCount(count).setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setResource(resource);
        if (StringUtil.isNotBlank(limitApp)) {
            rule.setLimitApp(limitApp);
        }
        FlowRuleManager.loadRules(Collections.singletonList(rule));
    }

    private void configureExceptionRulesFor(String resource, int count, String limitApp) {
        FlowRule rule = new FlowRule().setCount(count).setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_RATIO);
        rule.setResource(resource);
        if (StringUtil.isNotBlank(limitApp)) {
            rule.setLimitApp(limitApp);
        }
        FlowRuleManager.loadRules(Collections.singletonList(rule));
    }

    private void configureExceptionDegradeRulesFor(String resource, double count, String limitApp) {
        DegradeRule rule = new DegradeRule().setCount(count)
                .setStatIntervalMs(1000).setMinRequestAmount(1)
                .setTimeWindow(5).setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT);
        rule.setResource(resource);
        if (StringUtil.isNotBlank(limitApp)) {
            rule.setLimitApp(limitApp);
        }
        DegradeRuleManager.loadRules(Collections.singletonList(rule));
    }

    @After
    public void cleanUp() {
        FlowRuleManager.loadRules(null);
        DegradeRuleManager.loadRules(null);
        ClusterBuilderSlot.resetClusterNodes();
    }
}
