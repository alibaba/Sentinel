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

import com.alibaba.csp.sentinel.adapter.jdbc.delegate.DataSourceDelegate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Function;

/**
 * if you want use sentinel jdbc SQL flow control, you can do the following:
 * DataSource sentinelJDBCDataSource = new SentinelJDBCDataSource(datasource);
 * Connection connection = sentinelJDBCDataSource.getConnection();
 * ...............operate your db................
 *
 * @author icodening
 * @date 2022.02.08
 */
public class SentinelJDBCDataSource extends DataSourceDelegate {

    private Function<String, String> sqlMapper;

    public SentinelJDBCDataSource(DataSource dataSource) {
        super(dataSource);
    }

    public SentinelJDBCDataSource(DataSource dataSource, Function<String, String> sqlMapper) {
        super(dataSource);
        this.sqlMapper = sqlMapper;
    }

    public void setResourceNameMapper(Function<String, String> sqlMapper) {
        this.sqlMapper = sqlMapper;
    }

    @Override
    public Connection getConnection() throws SQLException {
        Connection connection = super.getConnection();
        return connection instanceof SentinelJDBCConnection ? connection : new SentinelJDBCConnection(connection, sqlMapper);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        Connection connection = super.getConnection(username, password);
        return connection instanceof SentinelJDBCConnection ? connection : new SentinelJDBCConnection(connection, sqlMapper);
    }
}
