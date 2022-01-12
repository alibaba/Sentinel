/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
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
import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.SentinelProperty;

/**
 * The abstract readable data source provides basic functionality for loading and parsing config.
 *
 * @param <D> data type for DataSource
 * @param <S> data type for Sentinel
 *
 * @author Carpenter Lee
 * @author Eric Zhao
 * @author Jiajiangnan
 */
public abstract class AbstractReadableDataSource<D, S> implements ReadableDataSource<D, S>{

    protected final AbstractDataSourceContext<D, S> context;
    protected final SentinelConverter<D, S> converter;
    protected final SentinelProperty<S> property;

    public AbstractReadableDataSource(AbstractDataSourceContext<D, S> context) {
        this.context = context;
        this.converter = context.getConverter();
        if (this.converter == null) {
            throw new IllegalArgumentException("converter can't be null");
        }
        this.property = new DynamicSentinelProperty<S>();
    }

    @Override
    public S loadConfig() throws Exception {
        return loadConfig(readSource());
    }

    public S loadConfig(D conf) throws Exception {
        S value = converter.toSentinel(conf);
        return value;
    }

    @Override
    public SentinelProperty<S> getProperty() {
        return property;
    }

}
