/*
 * Copyright 1999-2020 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
