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
package com.alibaba.csp.sentinel.adapter.mybatis;

import com.alibaba.csp.sentinel.adapter.mybatis.mapper.TeacherMapper;
import com.alibaba.csp.sentinel.adapter.mybatis.mapper.UserMapper;
import com.alibaba.csp.sentinel.adapter.mybatis.service.UserService;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.clusterbuilder.ClusterBuilderSlot;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.junit.After;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Collections;

/**
 * @author kaizi2009
 */
@RunWith(SpringRunner.class)
@SpringBootTest(classes = TestMybatisApplication.class)
@AutoConfigureMockMvc
public class BaseJunit {
    public static final Integer ID_1 = 1;
    public static final String USER_MAPPER_CLASS_NAME = UserMapper.class.getName();
    public static final String USER_RESOURCE_NAME_SELECT = USER_MAPPER_CLASS_NAME + ".selectById";
    public static final String USER_RESOURCE_NAME_UPDATE = USER_MAPPER_CLASS_NAME + ".update";
    public static final String USER_RESOURCE_NAME_INSERT = USER_MAPPER_CLASS_NAME + ".insert";
    public static final String USER_RESOURCE_NAME_DELETE = USER_MAPPER_CLASS_NAME + ".delete";
    public static final String TEACHER_RESOURCE_NAME_DELETE = TeacherMapper.class.getName() + ".delete";
    @Autowired
    protected UserMapper userMapper;
    @Autowired
    protected TeacherMapper teacherMapper;
    @Autowired
    protected UserService userService;

    protected void configureRulesFor(String resource, int count) {
        configureRulesFor(resource, count, "default");
    }

    protected void configureRulesFor(String resource, int count, String limitApp) {
        FlowRule rule = new FlowRule()
                .setCount(count)
                .setGrade(RuleConstant.FLOW_GRADE_QPS);
        rule.setResource(resource);
        if (StringUtil.isNotBlank(limitApp)) {
            rule.setLimitApp(limitApp);
        }
        FlowRuleManager.loadRules(Collections.singletonList(rule));
    }

    protected void configureExceptionRulesFor(String resource, int count) {
        DegradeRule rule = new DegradeRule()
                .setCount(count)
                .setGrade(RuleConstant.DEGRADE_GRADE_EXCEPTION_COUNT)
                .setMinRequestAmount(1)
                .setTimeWindow(10);
        rule.setResource(resource);
        DegradeRuleManager.loadRules(Collections.singletonList(rule));
    }

    @After
    public void cleanUp() {
        FlowRuleManager.loadRules(null);
        DegradeRuleManager.loadRules(null);
        ClusterBuilderSlot.resetClusterNodes();
    }
}
