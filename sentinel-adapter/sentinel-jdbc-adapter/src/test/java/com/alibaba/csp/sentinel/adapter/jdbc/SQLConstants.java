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

/**
 * @author icodening
 * @date 2022.02.09
 */
public class SQLConstants {

    public static final String SQL_INSERT_ONE = "insert into `student`(`id`, `name`, `create_time`) values (?, ?, ?)";

    public static final String SQL_DELETE_ONE = "delete from student where id = ?";

    public static final String SQL_UPDATE_ONE = "update student set name = ? where id = ?";

    public static final String SQL_SELECT_ONE = "select * from student where id = ?";

    public static final String SQL_SELECT_ALL = "select * from student";
}
