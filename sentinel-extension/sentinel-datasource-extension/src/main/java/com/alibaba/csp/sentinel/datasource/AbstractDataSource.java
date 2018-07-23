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

import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.SentinelProperty;

public abstract class AbstractDataSource<S, T> implements DataSource<S, T> {

    protected ConfigParser<S, T> parser;
    protected SentinelProperty<T> property;

    public AbstractDataSource(ConfigParser<S, T> parser) {
        if (parser == null) {
            throw new IllegalArgumentException("parser can't be null");
        }
        this.parser = parser;
        this.property = new DynamicSentinelProperty<T>();
    }

    @Override
    public T loadConfig() throws Exception {
        S readValue = readSource();
        T value = parser.parse(readValue);
        return value;
    }

    public T loadConfig(S conf) throws Exception {
        T value = parser.parse(conf);
        return value;
    }

    @Override
    public SentinelProperty<T> getProperty() {
        return property;
    }

    @Override
    public void writeDataSource(T values) throws Exception {
        throw new UnsupportedOperationException();
    }

}
