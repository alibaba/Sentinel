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

import com.alibaba.csp.sentinel.property.SentinelProperty;

/**
 * This class is responsible for getting configs.
 *
 * @param <S> source data type
 * @param <T> target data type
 * @author leyou
 */
public interface DataSource<S, T> {

    /**
     * Load data data source as the target type.
     *
     * @return the target data.
     * @throws Exception
     */
    T loadConfig() throws Exception;

    /**
     * Read original data from the data source.
     *
     * @return the original data.
     * @throws Exception
     */
    S readSource() throws Exception;

    /**
     * Get {@link SentinelProperty} of the data source.
     *
     * @return the property.
     */
    SentinelProperty<T> getProperty();

    /**
     * Write the {@code values} to the data source.
     *
     * @param values
     * @throws Exception
     */
    void writeDataSource(T values) throws Exception;

    /**
     * Close the data source.
     *
     * @throws Exception
     */
    void close() throws Exception;
}
