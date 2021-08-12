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
package com.alibaba.csp.sentinel.demo.mybatis.mapper;

import com.alibaba.csp.sentinel.demo.mybatis.po.TeacherPO;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

/**
 * @author kaizi2009
 */
@Repository
public interface TeacherMapper {

    @Select("select * from t_teacher where id = #{id}")
    @Results(id = "BaseResultMap", value = {
            @Result(column = "id", property = "id"),
            @Result(column = "name", property = "name"),
    })
    TeacherPO selectById(Integer id);

    @Delete("delete from t_teacher where\n id = #{id}")
    void delete(Integer id);

    @Insert("insert into t_teacher\n (id, name) values (#{id}, #{name})")
    void insert(TeacherPO teacher);

    @Select("select name, email from t_teacher where\n id = #{id}")
    TeacherPO testSqlException(Integer id);
}
