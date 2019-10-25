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
package com.alibaba.csp.sentinel.adapter.mybatis.test;

import com.alibaba.csp.sentinel.adapter.mybatis.SentinelTotalInterceptor;
import com.alibaba.csp.sentinel.adapter.mybatis.po.UserPO;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;
import org.junit.Test;
import org.mybatis.spring.MyBatisSystemException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.BadSqlGrammarException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author kaizi2009
 */
public class TestMapperInterceptor extends BaseJunit {
    private Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void testMybatisMapper() {
        testSelect();
        testUpdate();
        testInsert();
        testDelete();
    }

    public void testSelect() {
        String resourceName = USER_RESOURCE_NAME_SELECT;
        int limitCount = 2;
        configureRulesFor(resourceName, limitCount);

        int repeat = limitCount;
        for (int i = 0; i < repeat; i++) {
            UserPO user = userMapper.selectById(ID_1);
            assertNotNull(user);
            assertEquals(ID_1, user.getId());
        }
        ClusterNode cn = ClusterBuilderSlot.getClusterNode(resourceName);
        assertNotNull(cn);
        assertEquals(limitCount, cn.passQps(), 0.01);
        assertEquals(0, cn.blockQps(), 0.01);

        //Will be limited
        try {
            userMapper.selectById(ID_1);
        } catch (MyBatisSystemException e) {
            BlockException blockException = BlockException.getBlockException(e);
            assertNotNull(blockException);
        }

        //Limited assert
        cn = ClusterBuilderSlot.getClusterNode(resourceName);
        assertNotNull(cn);
        assertEquals(limitCount, cn.passQps(), 0.01);
        assertEquals(1, cn.blockQps(), 0.01);
    }

    public void testInsert() {
        String resourceName = USER_RESOURCE_NAME_INSERT;
        int limitCount = 2;
        configureRulesFor(resourceName, limitCount);

        int repeat = limitCount;
        UserPO user = new UserPO();
        user.setId(ID_1);
        user.setName("name new");
        for (int i = 0; i < repeat; i++) {
            userMapper.insert(user);
        }
        ClusterNode cn = ClusterBuilderSlot.getClusterNode(resourceName);
        assertNotNull(cn);
        assertEquals(limitCount, cn.passQps(), 0.01);
        assertEquals(0, cn.blockQps(), 0.01);

        //Will be limited
        try {
            userMapper.insert(user);
        } catch (MyBatisSystemException e) {
            BlockException blockException = BlockException.getBlockException(e);
            assertNotNull(blockException);
        }

        //Limited assert
        cn = ClusterBuilderSlot.getClusterNode(resourceName);
        assertNotNull(cn);
        assertEquals(limitCount, cn.passQps(), 0.01);
        assertEquals(1, cn.blockQps(), 0.01);
    }

    public void testUpdate() {
        String resourceName = USER_RESOURCE_NAME_UPDATE;
        int limitCount = 2;
        configureRulesFor(resourceName, limitCount);

        int repeat = limitCount;
        UserPO user = new UserPO();
        user.setId(ID_1);
        user.setName("name new");
        for (int i = 0; i < repeat; i++) {
            userMapper.update(user);
        }
        ClusterNode cn = ClusterBuilderSlot.getClusterNode(resourceName);
        assertNotNull(cn);
        assertEquals(limitCount, cn.passQps(), 0.01);
        assertEquals(0, cn.blockQps(), 0.01);

        //Will be limited
        try {
            userMapper.update(user);
        } catch (MyBatisSystemException e) {
            BlockException blockException = BlockException.getBlockException(e);
            assertNotNull(blockException);
        }

        //Limited assert
        cn = ClusterBuilderSlot.getClusterNode(resourceName);
        assertNotNull(cn);
        assertEquals(limitCount, cn.passQps(), 0.01);
        assertEquals(1, cn.blockQps(), 0.01);
    }

    public void testDelete() {
        String resourceName = USER_RESOURCE_NAME_DELETE;
        int limitCount = 2;
        configureRulesFor(resourceName, limitCount);

        int repeat = limitCount;
        for (int i = 0; i < repeat; i++) {
            userMapper.delete(ID_1);
        }
        ClusterNode cn = ClusterBuilderSlot.getClusterNode(resourceName);
        assertNotNull(cn);
        assertEquals(limitCount, cn.passQps(), 0.01);
        assertEquals(0, cn.blockQps(), 0.01);

        //Will be limited
        try {
            userMapper.delete(ID_1);
        } catch (MyBatisSystemException e) {
            BlockException blockException = BlockException.getBlockException(e);
            assertNotNull(blockException);
        }

        //Limited assert
        cn = ClusterBuilderSlot.getClusterNode(resourceName);
        assertNotNull(cn);
        assertEquals(limitCount, cn.passQps(), 0.01);
        assertEquals(1, cn.blockQps(), 0.01);
    }

    @Test
    public void testSqlException() {
        String resourceName = SentinelTotalInterceptor.RESOURCE_NAME;
        configureExceptionRulesFor(resourceName, 1);
        try {
            teacherMapper.testSqlException(ID_1);
        } catch (BadSqlGrammarException e) {
            logger.error("BadSqlGrammarException", e.getMessage());
        }

        //Will be limited
        try {
            teacherMapper.testSqlException(ID_1);
        } catch (MyBatisSystemException e) {
            BlockException blockException = BlockException.getBlockException(e);
            assertNotNull(blockException);
        }

        //Limited assert
        ClusterNode cn = ClusterBuilderSlot.getClusterNode(resourceName);
        assertNotNull(cn);
        assertEquals(1, cn.passQps(), 0.01);
        assertEquals(1, cn.blockQps(), 0.01);
    }

}

