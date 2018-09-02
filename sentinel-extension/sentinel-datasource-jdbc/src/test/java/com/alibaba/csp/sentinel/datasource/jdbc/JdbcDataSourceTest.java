package com.alibaba.csp.sentinel.datasource.jdbc;

import com.alibaba.csp.sentinel.datasource.DataSource;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.junit.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

/**
 * simple test for JdbcDataSource
 * @author five
 * @date 2018-09-01
 */
public class JdbcDataSourceTest {

    @Test
    public void testJdbcDataSource() {
        // mock JdbcTemplate
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);

        // mock db app_id=1
        when(jdbcTemplate.query(anyString(), (ResultSetExtractor<Integer>) any(), any())).thenReturn(1);

        // mock db flow rule List<Map<String, Object>>
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        Map<String, Object> map = new HashMap<String, Object>();
        list.add(map);
        map.put("resource", "com.xxx.FooService:hello(java.lang.String)");
        map.put("limit_app", "default");
        map.put("grade", RuleConstant.FLOW_GRADE_QPS);
        map.put("_count", 5d);
        map.put("strategy", RuleConstant.STRATEGY_DIRECT);
        map.put("control_behavior", RuleConstant.CONTROL_BEHAVIOR_DEFAULT);
        when(jdbcTemplate.queryForList(anyString(), any())).thenReturn(list);

        // appName
        String appName = "demo.service";

        // fresh per 3 seconds
        Long ruleRefreshSec = 3L;
        DataSource<List<Map<String, Object>>, List<FlowRule>> dataSource = new JdbcDataSource(jdbcTemplate, appName, new JdbcDataSource.JdbcFlowRuleParser(), ruleRefreshSec);
        FlowRuleManager.register2Property(dataSource.getProperty());

        // initAppId() invoke only 1 times
        verify(jdbcTemplate, times(1)).query(anyString(), (ResultSetExtractor<Integer>) any(), any());
        // readSource() invoke at least 1 times
        verify(jdbcTemplate, atLeastOnce()).queryForList(anyString(), any());

        List<FlowRule> flowRules = FlowRuleManager.getRules();
        assertTrue(flowRules.size() == 1);

        for (FlowRule flowRule : flowRules) {
            print(flowRule);

            assertEquals(RuleConstant.FLOW_GRADE_QPS, flowRule.getGrade());
            assertEquals(5d, flowRule.getCount(), 0d);
            assertEquals(RuleConstant.STRATEGY_DIRECT, flowRule.getStrategy());
            assertEquals(RuleConstant.CONTROL_BEHAVIOR_DEFAULT, flowRule.getControlBehavior());
        }

        // mock modify db value
        map.put("_count", 10d);

        // wait 5 seconds
        sleep(5000);

        // now, readSource() should invoke at least 2 times
        verify(jdbcTemplate, atLeast(2)).queryForList(anyString(), any());

        flowRules = FlowRuleManager.getRules();
        for (FlowRule flowRule : flowRules) {
            print(flowRule);

            assertEquals(10d, flowRule.getCount(), 0d);
        }
    }

    private void print(Object object) {
        System.out.println(object);
    }

    private void sleep(long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            throw new RuntimeException("Thread.sleep InterruptedException", e);
        }
    }
}
