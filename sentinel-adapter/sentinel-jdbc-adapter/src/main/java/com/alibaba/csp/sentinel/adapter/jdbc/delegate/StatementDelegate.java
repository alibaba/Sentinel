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
package com.alibaba.csp.sentinel.adapter.jdbc.delegate;

import java.sql.*;
import java.util.function.Function;

/**
 * @author icodening
 * @date 2022.02.09
 */
public class StatementDelegate extends WrapperDelegate<Statement> implements Statement {

    /**
     * default sql mapper, return origin sql
     */
    private static final Function<String, String> DEFAULT_SQL_MAPPER = (sql)->sql;

    private static final String KEY_SQL = "SQL";

    private Function<String, String> sqlMapper = DEFAULT_SQL_MAPPER;

    public StatementDelegate(Statement delegate) {
        super(delegate);
    }

    public void setSQLMapper(Function<String, String> sqlMapper) {
        this.sqlMapper = sqlMapper;
    }

    public Function<String, String> getSQLMapper() {
        return sqlMapper;
    }

    @Override
    protected String getResourceName() {
        return getSQL();
    }

    public void setSQL(String sql) {
        try {
            //users can customize the resource name
            if (getSQLMapper() != null) {
                setAttachment(KEY_SQL, getSQLMapper().apply(sql));
                return;
            }
        } catch (Throwable ignore) {
        }
        setAttachment(KEY_SQL, sql);
    }

    public String getSQL() {
        return getAttachment(KEY_SQL);
    }

    public ResultSet executeQuery(String sql) throws SQLException {
        return getDelegate().executeQuery(sql);
    }

    public int executeUpdate(String sql) throws SQLException {
        return getDelegate().executeUpdate(sql);
    }

    public void close() throws SQLException {
        getDelegate().close();
    }

    public int getMaxFieldSize() throws SQLException {
        return getDelegate().getMaxFieldSize();
    }

    public void setMaxFieldSize(int max) throws SQLException {
        getDelegate().setMaxFieldSize(max);
    }

    public int getMaxRows() throws SQLException {
        return getDelegate().getMaxRows();
    }

    public void setMaxRows(int max) throws SQLException {
        getDelegate().setMaxRows(max);
    }

    public void setEscapeProcessing(boolean enable) throws SQLException {
        getDelegate().setEscapeProcessing(enable);
    }

    public int getQueryTimeout() throws SQLException {
        return getDelegate().getQueryTimeout();
    }

    public void setQueryTimeout(int seconds) throws SQLException {
        getDelegate().setQueryTimeout(seconds);
    }

    public void cancel() throws SQLException {
        getDelegate().cancel();
    }

    public SQLWarning getWarnings() throws SQLException {
        return getDelegate().getWarnings();
    }

    public void clearWarnings() throws SQLException {
        getDelegate().clearWarnings();
    }

    public void setCursorName(String name) throws SQLException {
        getDelegate().setCursorName(name);
    }

    public boolean execute(String sql) throws SQLException {
        return getDelegate().execute(sql);
    }

    public ResultSet getResultSet() throws SQLException {
        return getDelegate().getResultSet();
    }

    public int getUpdateCount() throws SQLException {
        return getDelegate().getUpdateCount();
    }

    public boolean getMoreResults() throws SQLException {
        return getDelegate().getMoreResults();
    }

    public void setFetchDirection(int direction) throws SQLException {
        getDelegate().setFetchDirection(direction);
    }

    public int getFetchDirection() throws SQLException {
        return getDelegate().getFetchDirection();
    }

    public void setFetchSize(int rows) throws SQLException {
        getDelegate().setFetchSize(rows);
    }

    public int getFetchSize() throws SQLException {
        return getDelegate().getFetchSize();
    }

    public int getResultSetConcurrency() throws SQLException {
        return getDelegate().getResultSetConcurrency();
    }

    public int getResultSetType() throws SQLException {
        return getDelegate().getResultSetType();
    }

    public void addBatch(String sql) throws SQLException {
        getDelegate().addBatch(sql);
    }

    public void clearBatch() throws SQLException {
        getDelegate().clearBatch();
    }

    public int[] executeBatch() throws SQLException {
        return getDelegate().executeBatch();
    }

    public Connection getConnection() throws SQLException {
        return getDelegate().getConnection();
    }

    public boolean getMoreResults(int current) throws SQLException {
        return getDelegate().getMoreResults(current);
    }

    public ResultSet getGeneratedKeys() throws SQLException {
        return getDelegate().getGeneratedKeys();
    }

    public int executeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return getDelegate().executeUpdate(sql, autoGeneratedKeys);
    }

    public int executeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return getDelegate().executeUpdate(sql, columnIndexes);
    }

    public int executeUpdate(String sql, String[] columnNames) throws SQLException {
        return getDelegate().executeUpdate(sql, columnNames);
    }

    public boolean execute(String sql, int autoGeneratedKeys) throws SQLException {
        return getDelegate().execute(sql, autoGeneratedKeys);
    }

    public boolean execute(String sql, int[] columnIndexes) throws SQLException {
        return getDelegate().execute(sql, columnIndexes);
    }

    public boolean execute(String sql, String[] columnNames) throws SQLException {
        return getDelegate().execute(sql, columnNames);
    }

    public int getResultSetHoldability() throws SQLException {
        return getDelegate().getResultSetHoldability();
    }

    public boolean isClosed() throws SQLException {
        return getDelegate().isClosed();
    }

    public void setPoolable(boolean poolable) throws SQLException {
        getDelegate().setPoolable(poolable);
    }

    public boolean isPoolable() throws SQLException {
        return getDelegate().isPoolable();
    }

    public void closeOnCompletion() throws SQLException {
        getDelegate().closeOnCompletion();
    }

    public boolean isCloseOnCompletion() throws SQLException {
        return getDelegate().isCloseOnCompletion();
    }

    public long getLargeUpdateCount() throws SQLException {
        return getDelegate().getLargeUpdateCount();
    }

    public void setLargeMaxRows(long max) throws SQLException {
        getDelegate().setLargeMaxRows(max);
    }

    public long getLargeMaxRows() throws SQLException {
        return getDelegate().getLargeMaxRows();
    }

    public long[] executeLargeBatch() throws SQLException {
        return getDelegate().executeLargeBatch();
    }

    public long executeLargeUpdate(String sql) throws SQLException {
        return getDelegate().executeLargeUpdate(sql);
    }

    public long executeLargeUpdate(String sql, int autoGeneratedKeys) throws SQLException {
        return getDelegate().executeLargeUpdate(sql, autoGeneratedKeys);
    }

    public long executeLargeUpdate(String sql, int[] columnIndexes) throws SQLException {
        return getDelegate().executeLargeUpdate(sql, columnIndexes);
    }

    public long executeLargeUpdate(String sql, String[] columnNames) throws SQLException {
        return getDelegate().executeLargeUpdate(sql, columnNames);
    }
}
