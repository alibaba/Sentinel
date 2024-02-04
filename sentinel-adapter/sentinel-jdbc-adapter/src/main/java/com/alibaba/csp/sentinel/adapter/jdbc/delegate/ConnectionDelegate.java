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
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;

/**
 * @author icodening
 * @date 2022.02.08
 */
public class ConnectionDelegate extends WrapperDelegate<Connection> implements Connection{

    public ConnectionDelegate(Connection delegate) {
        super(delegate);
    }

    public Statement createStatement() throws SQLException {
        return getDelegate().createStatement();
    }

    public PreparedStatement prepareStatement(String sql) throws SQLException {
        return getDelegate().prepareStatement(sql);
    }

    public CallableStatement prepareCall(String sql) throws SQLException {
        return getDelegate().prepareCall(sql);
    }

    public String nativeSQL(String sql) throws SQLException {
        return getDelegate().nativeSQL(sql);
    }

    public void setAutoCommit(boolean autoCommit) throws SQLException {
        getDelegate().setAutoCommit(autoCommit);
    }

    public boolean getAutoCommit() throws SQLException {
        return getDelegate().getAutoCommit();
    }

    public void commit() throws SQLException {
        getDelegate().commit();
    }

    public void rollback() throws SQLException {
        getDelegate().rollback();
    }

    public void close() throws SQLException {
        getDelegate().close();
    }

    public boolean isClosed() throws SQLException {
        return getDelegate().isClosed();
    }

    public DatabaseMetaData getMetaData() throws SQLException {
        return getDelegate().getMetaData();
    }

    public void setReadOnly(boolean readOnly) throws SQLException {
        getDelegate().setReadOnly(readOnly);
    }

    public boolean isReadOnly() throws SQLException {
        return getDelegate().isReadOnly();
    }

    public void setCatalog(String catalog) throws SQLException {
        getDelegate().setCatalog(catalog);
    }

    public String getCatalog() throws SQLException {
        return getDelegate().getCatalog();
    }

    public void setTransactionIsolation(int level) throws SQLException {
        getDelegate().setTransactionIsolation(level);
    }

    public int getTransactionIsolation() throws SQLException {
        return getDelegate().getTransactionIsolation();
    }

    public SQLWarning getWarnings() throws SQLException {
        return getDelegate().getWarnings();
    }

    public void clearWarnings() throws SQLException {
        getDelegate().clearWarnings();
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency) throws SQLException {
        return getDelegate().createStatement(resultSetType, resultSetConcurrency);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return getDelegate().prepareStatement(sql, resultSetType, resultSetConcurrency);
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency) throws SQLException {
        return getDelegate().prepareCall(sql, resultSetType, resultSetConcurrency);
    }

    public Map<String, Class<?>> getTypeMap() throws SQLException {
        return getDelegate().getTypeMap();
    }

    public void setTypeMap(Map<String, Class<?>> map) throws SQLException {
        getDelegate().setTypeMap(map);
    }

    public void setHoldability(int holdability) throws SQLException {
        getDelegate().setHoldability(holdability);
    }

    public int getHoldability() throws SQLException {
        return getDelegate().getHoldability();
    }

    public Savepoint setSavepoint() throws SQLException {
        return getDelegate().setSavepoint();
    }

    public Savepoint setSavepoint(String name) throws SQLException {
        return getDelegate().setSavepoint(name);
    }

    public void rollback(Savepoint savepoint) throws SQLException {
        getDelegate().rollback(savepoint);
    }

    public void releaseSavepoint(Savepoint savepoint) throws SQLException {
        getDelegate().releaseSavepoint(savepoint);
    }

    public Statement createStatement(int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return getDelegate().createStatement(resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public PreparedStatement prepareStatement(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return getDelegate().prepareStatement(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public CallableStatement prepareCall(String sql, int resultSetType, int resultSetConcurrency, int resultSetHoldability) throws SQLException {
        return getDelegate().prepareCall(sql, resultSetType, resultSetConcurrency, resultSetHoldability);
    }

    public PreparedStatement prepareStatement(String sql, int autoGeneratedKeys) throws SQLException {
        return getDelegate().prepareStatement(sql, autoGeneratedKeys);
    }

    public PreparedStatement prepareStatement(String sql, int[] columnIndexes) throws SQLException {
        return getDelegate().prepareStatement(sql, columnIndexes);
    }

    public PreparedStatement prepareStatement(String sql, String[] columnNames) throws SQLException {
        return getDelegate().prepareStatement(sql, columnNames);
    }

    public Clob createClob() throws SQLException {
        return getDelegate().createClob();
    }

    public Blob createBlob() throws SQLException {
        return getDelegate().createBlob();
    }

    public NClob createNClob() throws SQLException {
        return getDelegate().createNClob();
    }

    public SQLXML createSQLXML() throws SQLException {
        return getDelegate().createSQLXML();
    }

    public boolean isValid(int timeout) throws SQLException {
        return getDelegate().isValid(timeout);
    }

    public void setClientInfo(String name, String value) throws SQLClientInfoException {
        getDelegate().setClientInfo(name, value);
    }

    public void setClientInfo(Properties properties) throws SQLClientInfoException {
        getDelegate().setClientInfo(properties);
    }

    public String getClientInfo(String name) throws SQLException {
        return getDelegate().getClientInfo(name);
    }

    public Properties getClientInfo() throws SQLException {
        return getDelegate().getClientInfo();
    }

    public Array createArrayOf(String typeName, Object[] elements) throws SQLException {
        return getDelegate().createArrayOf(typeName, elements);
    }

    public Struct createStruct(String typeName, Object[] attributes) throws SQLException {
        return getDelegate().createStruct(typeName, attributes);
    }

    public void setSchema(String schema) throws SQLException {
        getDelegate().setSchema(schema);
    }

    public String getSchema() throws SQLException {
        return getDelegate().getSchema();
    }

    public void abort(Executor executor) throws SQLException {
        getDelegate().abort(executor);
    }

    public void setNetworkTimeout(Executor executor, int milliseconds) throws SQLException {
        getDelegate().setNetworkTimeout(executor, milliseconds);
    }

    public int getNetworkTimeout() throws SQLException {
        return getDelegate().getNetworkTimeout();
    }
}
