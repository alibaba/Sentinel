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

import com.alibaba.csp.sentinel.adapter.jdbc.delegate.PreparedStatementDelegate;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.function.Function;

/**
 * @author icodening
 * @date 2022.02.09
 */
public class SentinelJDBCPreparedStatement extends PreparedStatementDelegate {

    public SentinelJDBCPreparedStatement(PreparedStatement delegate) {
        super(delegate);
    }

    public SentinelJDBCPreparedStatement(PreparedStatement delegate,Function<String, String> resourceNameMapper) {
        super(delegate);
        setSQLMapper(resourceNameMapper);
    }

    @Override
    public ResultSet executeQuery() throws SQLException {
        return sentinelEntryInternal(super::executeQuery, true);
    }

    @Override
    public int executeUpdate() throws SQLException {
        return sentinelEntryInternal(super::executeUpdate, true);
    }

    @Override
    public boolean execute() throws SQLException {
        return sentinelEntryInternal(super::execute, true);
    }

    @Override
    public long executeLargeUpdate() throws SQLException {
        return sentinelEntryInternal(super::executeLargeUpdate, true);
    }
}
