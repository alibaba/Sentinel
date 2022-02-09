package com.alibaba.csp.sentinel.adapter.jdbc;


/**
 * @author icodening
 * @date 2022.02.09
 */
public interface JDBCContext {
    String JDBC_CONTEXT_NAME = "jdbc_context";

    String KEY_SQL = "SQL";

    void setAttachment(String name, Object object);

    <T> T getAttachment(String name, T defaultValue);

    <T> T getAttachmentWithCast(String name, Class<T> attachmentType);

    void clearAttachments();

    default <T> T getAttachment(String name) {
        return getAttachment(name, null);
    }

    default void setSQL(String sql) {
        setAttachment(KEY_SQL, sql);
    }

    default String getSQL() {
        return getAttachment(KEY_SQL);
    }
}
