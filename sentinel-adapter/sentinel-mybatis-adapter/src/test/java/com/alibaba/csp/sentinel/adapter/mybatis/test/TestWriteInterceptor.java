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

import com.alibaba.csp.sentinel.adapter.mybatis.SentinelWriteInterceptor;
import com.alibaba.csp.sentinel.adapter.mybatis.po.TeacherPO;
import com.alibaba.csp.sentinel.adapter.mybatis.po.UserPO;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;
import org.junit.Test;
import org.mybatis.spring.MyBatisSystemException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author kaizi2009
 */
public class TestWriteInterceptor extends BaseJunit {

    @Test
    public void testWrite() {
        String resourceName = SentinelWriteInterceptor.RESOURCE_NAME;
        configureRulesFor(resourceName, 2);

        UserPO user = new UserPO();
        user.setId(10);
        user.setName("name10");
        userMapper.insert(user);
        teacherMapper.delete(ID_1);

        //Will be limited
        TeacherPO teacher = new TeacherPO();
        teacher.setId(10);
        teacher.setName("name10");
        try {
            teacherMapper.insert(teacher);
        } catch (MyBatisSystemException e) {
            BlockException blockException = BlockException.getBlockException(e);
            assertNotNull(blockException);
        }

        //Assert
        ClusterNode cn = ClusterBuilderSlot.getClusterNode(resourceName);
        assertNotNull(cn);
        assertEquals(2, cn.passQps(), 0.01);
        assertEquals(1, cn.blockQps(), 0.01);

    }

}
