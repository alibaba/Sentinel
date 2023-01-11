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
package com.alibaba.csp.sentinel.demo.mybatis.controller;

import com.alibaba.csp.sentinel.demo.mybatis.mapper.TeacherMapper;
import com.alibaba.csp.sentinel.demo.mybatis.mapper.UserMapper;
import com.alibaba.csp.sentinel.demo.mybatis.po.TeacherPO;
import com.alibaba.csp.sentinel.demo.mybatis.po.UserPO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author kaizi2009
 */
@RestController
public class UserController {

    @Autowired
    private UserMapper userMapper;
    @Autowired
    private TeacherMapper teacherMapper;

    @GetMapping("/testSqlException")
    public TeacherPO query() {
        return teacherMapper.testSqlException(1);
    }

    @GetMapping("/getUser")
    public UserPO getUser() {
        return userMapper.selectById(1);
    }

}
