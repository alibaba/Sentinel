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
package com.alibaba.csp.sentinel.datasource.nacos;

import com.alibaba.csp.sentinel.datasource.AbstractWritableDataSource;
import com.alibaba.csp.sentinel.datasource.DataSourceHolder;
import com.alibaba.csp.sentinel.datasource.converter.SentinelConverter;
import com.alibaba.nacos.api.config.ConfigService;

import java.util.Properties;

/**
 * A write-only {@code DataSource} with Nacos backend. Publish the data to Nacos,
 * so that Nacos will automatically push the new value so that the dynamic configuration can be real-time.
 *
 * @param <T> target data type for Sentinel
 *
 * @author Jiajiangnan
 */
public class NacosWritableDataSource<T> extends AbstractWritableDataSource<T> {

    private final String groupId;
    private final String dataId;

    private final SentinelConverter<String, T> converter;

    private ConfigService configService = null;

    public NacosWritableDataSource(final Properties properties, final String groupId, final String dataId, final DataSourceHolder dataSourceHolder) {
        super(dataSourceHolder);
        this.groupId = groupId;
        this.dataId = dataId;
        this.converter = dataSourceHolder.getConverter();

        init();
    }

    private void init() {
        this.configService = dataSourceHolder.getDataSourceClient() == null ? null : (ConfigService) dataSourceHolder.getDataSourceClient();
    }

    @Override
    public void write(T value) throws Exception {
        String content = converter.fromSentinel(value);
        configService.publishConfig(dataId, groupId, content);
    }

    @Override
    public void close() {
        // Nothing to do
    }

}
