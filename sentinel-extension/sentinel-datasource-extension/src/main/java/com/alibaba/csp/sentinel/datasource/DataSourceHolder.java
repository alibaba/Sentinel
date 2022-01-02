package com.alibaba.csp.sentinel.datasource;

import com.alibaba.csp.sentinel.datasource.converter.SentinelConverter;
import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.SentinelProperty;

/**
 * DataSourceHolder who holds some Object in Sentinel-Context where be used for ReadableDataSource and WritableDataSource
 *
 * @param <S> source data type for DataSource
 * @param <T> target data type for Sentinel
 *
 * @author Jiajiangnan
 */
public class DataSourceHolder<S, T, C> {

    protected final SentinelConverter<S, T> converter;
    protected final DataSourceMode dataSourceMode;
    protected final SentinelProperty<T> property;
    private C client;

    public DataSourceHolder(final SentinelConverter<S, T> converter, final DataSourceMode dataSourceMode) {
        if (converter == null) {
            throw new IllegalArgumentException("parser can't be null");
        }
        this.converter = converter;
        this.dataSourceMode = dataSourceMode;
        this.property = new DynamicSentinelProperty<T>();
    }

    protected void setDataSourceClient(C client) {
        this.client = client;
    }

    public C getDataSourceClient() {
        return this.client;
    }

    public SentinelConverter<S, T> getConverter() {
        return this.converter;
    }

    public SentinelProperty<T> getProperty() {
        return this.property;
    }

}
