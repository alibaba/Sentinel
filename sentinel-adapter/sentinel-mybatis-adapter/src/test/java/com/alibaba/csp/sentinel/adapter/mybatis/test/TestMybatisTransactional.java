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

import com.alibaba.csp.sentinel.adapter.mybatis.SentinelMapperInterceptor;
import com.alibaba.csp.sentinel.adapter.mybatis.po.TeacherPO;
import com.alibaba.csp.sentinel.adapter.mybatis.po.UserPO;
import com.alibaba.csp.sentinel.node.ClusterNode;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;
import org.junit.Test;
import org.mybatis.spring.MyBatisSystemException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * @author kaizi2009
 */
public class TestMybatisTransactional extends BaseJunit {

    @Test
    public void testTransactional() {
        configureRulesFor(TEACHER_RESOURCE_NAME_DELETE, 1);

        userService.deleteUserAndTeacher(2, 2);

        //Will be limited
        int deleteUserId = 3;
        int deleteTeacherId = 3;
        try {
            userService.deleteUserAndTeacher(deleteUserId, deleteTeacherId);
        } catch (MyBatisSystemException e) {
            BlockException blockException = BlockException.getBlockException(e);
            assertNotNull(blockException);
        }

        //limited assert
        ClusterNode cn = ClusterBuilderSlot.getClusterNode(TEACHER_RESOURCE_NAME_DELETE);
        assertNotNull(cn);
        assertEquals(1, cn.passQps(), 0.01);
        assertEquals(1, cn.blockQps(), 0.01);

        //Rollback assert
        UserPO user = userMapper.selectById(deleteUserId);
        assertNotNull(user);
        TeacherPO teacher = teacherMapper.selectById(deleteTeacherId);
        assertNotNull(teacher);
    }


}
