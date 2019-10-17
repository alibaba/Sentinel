package com.alibaba.csp.sentinel.adapter.spring.webmvc;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;
import com.alibaba.csp.sentinel.util.StringUtil;
import java.util.Collections;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

/**
 * @author zhangkai
 * @description
 * @date 2019-10-15
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestApplication.class)
@AutoConfigureMockMvc
public class TestAop {

    private Logger logger = LoggerFactory.getLogger(this.getClass());
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
    public void testSpringMvc() throws Exception {
        testBase();
        testOriginParser();
    }

    private void testBase() throws Exception {
        String url = "/hello";
        this.mvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(content().string(HELLO_STR));

        ClusterNode cn = ClusterBuilderSlot.getClusterNode(url);
        assertNotNull(cn);
        assertEquals(1, cn.passQps(), 0.01);
    }

    private void testOriginParser() throws Exception {
        String springMvcPathVariableUrl = "/foo/{id}";
        String limitOrigin = "userA";
        final String headerName = "S-User";
        configureRulesFor(springMvcPathVariableUrl, 0, limitOrigin);

        this.mvc.perform(get("/foo/1").accept(MediaType.TEXT_PLAIN).header(headerName, "userB"))
                .andExpect(status().isOk())
                .andExpect(content().string("foo 1"));
        // This will be blocked and reponse json.
        this.mvc.perform(
                get("/foo/2").accept(MediaType.APPLICATION_JSON).header(headerName, limitOrigin))
                .andExpect(status().isOk())
                .andExpect(content().json(ResultWrapper.blocked().toJsonString()));
        this.mvc.perform(get("/foo/3").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(ResultWrapper.blocked().toJsonString()));

        FlowRuleManager.loadRules(null);
    }

    @After
    public void cleanUp() {
        FlowRuleManager.loadRules(null);
        ClusterBuilderSlot.resetClusterNodes();
    }
}
