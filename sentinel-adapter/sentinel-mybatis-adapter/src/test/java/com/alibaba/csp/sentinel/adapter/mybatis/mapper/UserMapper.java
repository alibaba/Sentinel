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
package com.alibaba.csp.sentinel.adapter.mybatis.mapper;

import com.alibaba.csp.sentinel.adapter.mybatis.po.UserPO;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

/**
 * @author kaizi2009
 */
@Repository
public interface UserMapper {

    @Select("select * from t_user where \n id = #{id}")
    @Results(id = "BaseResultMap", value = {
            @Result(column = "id", property = "id"),
            @Result(column = "name", property = "name"),
    })
    UserPO selectById(Integer id);

    @Select("select * from t_user where \n  id = #{id} and name = #{name}")
    UserPO selectByIdAndName(@Param("id") Integer id, @Param("name") String name);

    @Select("select * from t_user where \n  id = #{id} and name = #{name}")
    UserPO selectByIdAndName1(UserPO user);

    @Delete("delete from t_user where \n  id = #{id}")
    void delete(Integer id);

    @Insert("insert into t_user (id, name) values (#{id}, #{name})")
    void insert(UserPO user);

    @Update("update t_user set name = #{name} where id = #{id}")
    void update(UserPO user);
}
