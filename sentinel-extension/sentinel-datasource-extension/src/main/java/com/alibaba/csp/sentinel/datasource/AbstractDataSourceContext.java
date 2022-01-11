package com.alibaba.csp.sentinel.datasource;

import com.alibaba.csp.sentinel.datasource.converter.SentinelConverter;

/**
 * Abstract DataSourceContext which holds some common-object in Sentinel-Context that would be used by ReadableDataSource and WritableDataSource
 *
 * @param <D> data type for DataSource
 * @param <S> data type for Sentinel
 *
 * @author Jiajiangnan
 */
public abstract class AbstractDataSourceContext<D, S> {

    protected final Object client;
    protected final SentinelConverter<D, S> converter;
    protected final DataSoureMode dataSoureMode;

    public AbstractDataSourceContext(final Object client, final SentinelConverter<D, S> converter, final DataSoureMode dataSoureMode) {
        this.client = client;
        this.converter = converter;
        this.dataSoureMode = dataSoureMode;
    }

    public Object getClient() {
        return client;
    }

    public SentinelConverter<D, S> getConverter() {
        return converter;
    }

    public DataSoureMode getDataSourceMode() {
        return dataSoureMode;
    }
}
