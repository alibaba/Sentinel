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

import com.alibaba.csp.sentinel.property.NoOpSentinelProperty;
import com.alibaba.csp.sentinel.property.SentinelProperty;

/**
 * A {@link ReadableDataSource} based on nothing. {@link EmptyDataSource#getProperty()} will always return the same cached
 * {@link SentinelProperty} that doing nothing.
 * <br/>
 * This class is used when we want to use default settings instead of configs from the {@link ReadableDataSource}.
 *
 * @author leyou
 */
public final class EmptyDataSource implements ReadableDataSource<Object, Object> {

    public static final ReadableDataSource<Object, Object> EMPTY_DATASOURCE = new EmptyDataSource();

    private static final SentinelProperty<Object> PROPERTY = new NoOpSentinelProperty();

    private EmptyDataSource() { }

    @Override
    public Object loadConfig() throws Exception {
        return null;
    }

    @Override
    public Object readSource() throws Exception {
        return null;
    }

    @Override
    public SentinelProperty<Object> getProperty() {
        return PROPERTY;
    }

    @Override
    public void close() throws Exception { }
}
