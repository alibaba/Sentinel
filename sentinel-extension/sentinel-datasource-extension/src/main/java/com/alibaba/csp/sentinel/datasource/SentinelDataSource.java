package com.alibaba.csp.sentinel.datasource;

/**
 * Abstract SentinelDataSource
 *
 * @author Jiajiangnan
 */
public abstract class SentinelDataSource {

    public abstract ReadableDataSource getReader();

    public abstract WritableDataSource getWriter();

    public void closeDataSource() throws Exception {
        preDataSourceClose();
        this.getReader().close();
        this.getWriter().close();
        postDataSourceClose();
    }

    protected void postDataSourceClose() {

    }

    protected void preDataSourceClose() {
        
    };

}
