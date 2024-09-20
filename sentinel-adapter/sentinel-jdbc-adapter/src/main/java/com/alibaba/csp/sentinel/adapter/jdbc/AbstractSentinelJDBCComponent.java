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

import com.alibaba.csp.sentinel.Entry;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.ResourceTypeConstants;
import com.alibaba.csp.sentinel.SphU;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.alibaba.csp.sentinel.util.StringUtil;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author icodening
 * @date 2022.02.09
 */
public abstract class AbstractSentinelJDBCComponent implements JDBCContext {

    private final Map<String, Object> attachment = new HashMap<>(4);

    @Override
    public void setAttachment(String name, Object object) {
        attachment.put(name, object);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T getAttachment(String name, T defaultValue) {
        if (name == null) {
            return null;
        }
        return (T) attachment.get(name);
    }

    @Override
    public <T> T getAttachmentWithCast(String name, Class<T> attachmentType) {
        if (name == null || attachmentType == null) {
            return null;
        }
        Object retValue = attachment.get(name);
        if (retValue == null) {
            return null;
        }
        if (attachmentType.isAssignableFrom(retValue.getClass())) {
            return attachmentType.cast(retValue);
        }
        return null;
    }

    @Override
    public void clearAttachments() {
        attachment.clear();
    }

    protected <E> E sentinelEntryInternal(SQLExceptionCallable<E> callable) throws SQLException {
        return sentinelEntryInternal(callable, false);
    }

    protected <E> E sentinelEntryInternal(SQLExceptionCallable<E> callable, boolean clearAttachmentsOnComplete) throws SQLException {
        Entry entry = null;
        try {
            String resourceName = getResourceName();
            if (StringUtil.isBlank(resourceName)) {
                return callable.call();
            }
            entry = SphU.entry(resourceName, ResourceTypeConstants.COMMON_DB_SQL, EntryType.OUT);
            return callable.call();
        } catch (BlockException e) {
            //in Spring MyBatis, this SentinelSQLException will be wrapped as UncategorizedSQLException,
            // users should use UncategorizedSQLException.getCause() to get SentinelSQLException,
            // and SentinelSQLException.getBlockException() to get BlockException
            // -------------------
            //in Spring Data JPA, this SentinelSQLException will be wrapped as GenericJDBCException,
            // users should use GenericJDBCException.getSQLException() to get SentinelSQLException
            // and SentinelSQLException.getBlockException() to get BlockException
            throw new SentinelSQLException("too many request", e);
        } finally {
            if (clearAttachmentsOnComplete) {
                clearAttachments();
            }
            if (entry != null) {
                entry.exit();
            }
        }
    }

    /**
     * default return null
     *
     * @return sentinel resource name
     */
    protected String getResourceName() {
        return null;
    }

    @FunctionalInterface
    protected interface SQLExceptionCallable<V> {

        V call() throws SQLException;

    }
}
