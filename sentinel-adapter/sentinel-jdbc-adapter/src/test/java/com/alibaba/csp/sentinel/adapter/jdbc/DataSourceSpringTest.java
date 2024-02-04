/*
 * Copyright 1999-2022 Alibaba Group Holding Ltd.
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
package com.alibaba.csp.sentinel.adapter.jdbc;

import com.alibaba.csp.sentinel.adapter.jdbc.calcite.CalciteUtil;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import org.junit.Before;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author icodening
 * @date 2022.02.09
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {"classpath:spring-context.xml"})
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class DataSourceSpringTest {

    private static final double MAX_QPS = 2.0;

    @Autowired
    private DataSource dataSource;

    @Before
    public void setUp() {
        initFlowRules();
        this.dataSource = new SentinelJDBCDataSource(dataSource);
    }

    @Test
    public void testAInsertOne() {
        try (Connection connection = dataSource.getConnection()) {
            int result = update(connection, SQLConstants.SQL_INSERT_ONE, Arrays.asList(11L, "Spring Insert", new java.util.Date()));
            assertEquals(1, result);
            System.out.println("insert student success, count:" + result);
        } catch (Throwable e) {
            assertTrue(e instanceof SentinelSQLException);
        }
    }

    @Test
    public void testBDeleteOne() {
        try (Connection connection = dataSource.getConnection()) {
            int result = update(connection, SQLConstants.SQL_DELETE_ONE, Collections.singletonList(3L));
            assertEquals(1, result);
            System.out.println("delete student success, count:" + result);
        } catch (Throwable e) {
            assertTrue(e instanceof SentinelSQLException);
        }
    }

    @Test
    public void testCUpdateOne() {
        try (Connection connection = dataSource.getConnection()) {
            int result = update(connection, SQLConstants.SQL_UPDATE_ONE, Arrays.asList("Spring Update", 2L));
            assertEquals(1, result);
            System.out.println("insert student success, count:" + result);
        } catch (Throwable e) {
            assertTrue(e instanceof SentinelSQLException);
        }
    }

    @Test
    public void testDSelectOne() {
        try (Connection connection = dataSource.getConnection()) {
            ResultSet resultSet = query(connection, SQLConstants.SQL_SELECT_ONE, Collections.singletonList(1L));
            int result = 0;
            while (resultSet.next()) {
                ++result;
                long id = resultSet.getLong("id");
                String name = resultSet.getString("name");
                Date date = new Date(resultSet.getDate("create_time").getTime());
                System.out.println("Student {id=" + id + ", name=" + name + ", create_time=" + date + "}");
            }
            assertEquals(1, result);
        } catch (Throwable e) {
            assertTrue(e instanceof SentinelSQLException);
        }
    }

    @Test
    public void testESelectAll() {
        try (Connection connection = dataSource.getConnection()) {
            ResultSet resultSet = query(connection, SQLConstants.SQL_SELECT_ALL, Collections.emptyList());
            int result = 0;
            while (resultSet.next()) {
                ++result;
                long id = resultSet.getLong("id");
                String name = resultSet.getString("name");
                Date date = new Date(resultSet.getDate("create_time").getTime());
                System.out.println("Student {id=" + id + ", name=" + name + ", create_time=" + date + "}");
            }
            assertEquals(10, result);
        } catch (Throwable e) {
            assertTrue(e instanceof SentinelSQLException);
        }
    }

    @Test
    public void testFContinuousSelectOne() {
        for (int i = 0; i < 10; i++) {
            testDSelectOne();
        }
    }

    @Test
    public void testGContinuousNormalStatmentSelectOne() {
        for (int i = 0; i < 10; i++) {
            try (Connection connection = dataSource.getConnection()) {
                String sql = "select * from student where id = " + (i + 1);
                ResultSet resultSet = normalQuery(connection, sql);
                int result = 0;
                while (resultSet.next()) {
                    ++result;
                    long id = resultSet.getLong("id");
                    String name = resultSet.getString("name");
                    Date date = new Date(resultSet.getDate("create_time").getTime());
                    System.out.println("Student {id=" + id + ", name=" + name + ", create_time=" + date + "}");
                }
                assertEquals(1, result);
            } catch (Throwable e) {
                assertTrue(e instanceof SentinelSQLException);
            }
        }
    }

    private ResultSet query(Connection connection, String sql, List<Object> args) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(sql);
        setParams(ps, args);
        ResultSet ret = ps.executeQuery();
        connection.commit();
        return ret;
    }

    private ResultSet normalQuery(Connection connection, String sql) throws SQLException {
        Statement statement = connection.createStatement();
        ResultSet ret = statement.executeQuery(sql);
        connection.commit();
        return ret;
    }

    private int update(Connection connection, String sql, List<Object> args) throws SQLException {
        PreparedStatement ps = connection.prepareStatement(sql);
        setParams(ps, args);
        int ret = ps.executeUpdate();
        connection.commit();
        return ret;

    }

    private void setParams(PreparedStatement ps, List<Object> args) throws SQLException {
        if (!(args == null || args.isEmpty())) {
            for (int i = 0; i < args.size(); i++) {
                ps.setObject(i + 1, args.get(i));
            }
        }
    }


    private void initFlowRules() {
        FlowRule r1 = (FlowRule) new FlowRule()
                .setGrade(RuleConstant.FLOW_GRADE_QPS)
                .setCount(MAX_QPS)
                .setResource(SQLConstants.SQL_INSERT_ONE);

        FlowRule r2 = (FlowRule) new FlowRule()
                .setGrade(RuleConstant.FLOW_GRADE_QPS)
                .setCount(MAX_QPS)
                .setResource(SQLConstants.SQL_DELETE_ONE);

        FlowRule r3 = (FlowRule) new FlowRule()
                .setGrade(RuleConstant.FLOW_GRADE_QPS)
                .setCount(MAX_QPS)
                .setResource(SQLConstants.SQL_UPDATE_ONE);

        FlowRule r4 = (FlowRule) new FlowRule()
                .setGrade(RuleConstant.FLOW_GRADE_QPS)
                .setCount(MAX_QPS)
                .setResource(SQLConstants.SQL_SELECT_ONE);

        List<FlowRule> flowRules = new ArrayList<>(4);
        flowRules.add(r1);
        flowRules.add(r2);
        flowRules.add(r3);
        flowRules.add(r4);

        String sql = CalciteUtil.replaceSQLParametersWithoutException("select * from student where id = " + 1);
        FlowRule r5 = (FlowRule) new FlowRule()
                .setGrade(RuleConstant.FLOW_GRADE_QPS)
                .setCount(MAX_QPS)
                .setResource(sql);
        flowRules.add(r5);

        FlowRuleManager.loadRules(flowRules);
    }

}
