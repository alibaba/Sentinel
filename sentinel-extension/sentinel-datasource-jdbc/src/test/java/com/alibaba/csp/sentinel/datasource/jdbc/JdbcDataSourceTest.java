package com.alibaba.csp.sentinel.datasource.jdbc;

import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.junit.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

/**
 * a simple test for JdbcDataSource
 * @author cdfive
 * @date 2018-09-01
 */
public class JdbcDataSourceTest {

    @Test
    public void testJdbcDataSource() throws Exception {
        // mock javax.sql.Xxx Object
        javax.sql.DataSource dbDataSource = mock(javax.sql.DataSource.class);
        Connection connection = mock(Connection.class);
        PreparedStatement preparedStatement = mock(PreparedStatement.class);
        ResultSet resultSet = mock(ResultSet.class);
        ResultSetMetaData resultSetMetaData = mock(ResultSetMetaData.class);

        when(dbDataSource.getConnection()).thenReturn(connection);
        when(connection.prepareStatement(anyString())).thenReturn(preparedStatement);
        when(preparedStatement.executeQuery()).thenReturn(resultSet);
        when(resultSet.getMetaData()).thenReturn(resultSetMetaData);
        when(resultSetMetaData.getColumnCount()).thenReturn(6);// mock 6 columns
        when(resultSet.next()).thenReturn(true, false, true, false, true, false, true, false, true, false);// mock 5 findListMapBySql invokes, each query 1 row data

        // mock column label names
        final String[] columnLabels = new String[] {"resource", "limit_app", "grade", "_count", "strategy", "control_behavior"};
        for (int i = 0; i < columnLabels.length; i++) {
            when(resultSetMetaData.getColumnLabel(i + 1)).thenReturn(columnLabels[i]);
        }

        // mock column values
        final Object[] resultSetObjects = new Object[] {
            "com.xxx.FooService:hello(java.lang.String)"
          , "default"
          , RuleConstant.FLOW_GRADE_QPS
          , 5D
          , RuleConstant.STRATEGY_DIRECT
          , RuleConstant.CONTROL_BEHAVIOR_DEFAULT
        };
        for (int i = 0; i < resultSetObjects.length; i++) {
            when(resultSet.getObject(i + 1)).thenReturn(resultSetObjects[i]);
        }

        // mock sql
        String sql = "select * from xxx";

        // refresh per 3 seconds
        Long ruleRefreshSec = 3L;
        ReadableDataSource<List<Map<String, Object>>, List<FlowRule>> dataSource = new JdbcDataSource(dbDataSource, sql, null, new JdbcDataSource.JdbcFlowRuleConverter(), ruleRefreshSec);
        FlowRuleManager.register2Property(dataSource.getProperty());

        // preparedStatement.executeQuery should invoke at least 1 times
        verify(preparedStatement, atLeastOnce()).executeQuery();
        // resultSet.next should invoke at least 2 times
        verify(resultSet, atLeast(2)).next();

        List<FlowRule> flowRules = FlowRuleManager.getRules();
        assertTrue(flowRules.size() == 1);

        for (FlowRule flowRule : flowRules) {
            print(flowRule);

            assertEquals(RuleConstant.FLOW_GRADE_QPS, flowRule.getGrade());
            assertEquals(5D, flowRule.getCount(), 0D);
            assertEquals(RuleConstant.STRATEGY_DIRECT, flowRule.getStrategy());
            assertEquals(RuleConstant.CONTROL_BEHAVIOR_DEFAULT, flowRule.getControlBehavior());
        }

        // mock modify db value
        when(resultSet.getObject(4)).thenReturn(10D);

        // wait 5 seconds, waiting for refresh
        sleep(5000);

        // now, preparedStatement.executeQuery should invoke at least 2 times
        verify(preparedStatement, atLeast(2)).executeQuery();
        // now, resultSet.next should invoke at least 4 times
        verify(resultSet, atLeast(4)).next();

        flowRules = FlowRuleManager.getRules();
        for (FlowRule flowRule : flowRules) {
            print(flowRule);

            assertEquals(10D, flowRule.getCount(), 0d);// now flow rule's count should be 10D
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
