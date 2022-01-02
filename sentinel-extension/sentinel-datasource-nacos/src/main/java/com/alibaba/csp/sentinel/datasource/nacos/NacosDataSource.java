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

import com.alibaba.csp.sentinel.datasource.DataSourceHolder;
import com.alibaba.csp.sentinel.datasource.DataSourceMode;
import com.alibaba.csp.sentinel.datasource.converter.SentinelConverter;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;

import java.util.Properties;

/**
 * Extense DataSource for Nacos, NacosDataSource holds NacosReadableDataSource and NacosReadableDataSource.
 * When new an instance By constructor we can define the mode of DataSource like Readable, Writable, or All
 *
 * @param <T> target data type for Sentinel
 *
 * @author Jiajiangnan
 */
public class NacosDataSource<T> extends DataSourceHolder {

    private NacosReadableDataSource<T> readableDataSource;
    private NacosWritableDataSource<T> writableDataSource;

    /**
     * Constructs an read-only DataSource with Nacos backend.
     *
     * @param serverAddr server address of Nacos, cannot be empty
     * @param groupId    group ID, cannot be empty
     * @param dataId     data ID, cannot be empty
     * @param parser     customized data parser, cannot be empty
     */
    public NacosDataSource(final String serverAddr, final String groupId, final String dataId, SentinelConverter<String, T> parser) {
        this(NacosDataSource.buildProperties(serverAddr), groupId, dataId, parser, DataSourceMode.READABLE);
    }
    /**
     * Constructs an read-only DataSource with Nacos backend.
     *
     * @param serverAddr server address of Nacos, cannot be empty
     * @param groupId    group ID, cannot be empty
     * @param dataId     data ID, cannot be empty
     * @param parser     customized data parser, cannot be empty
     * @param dataSourceMode     dataSourceMode, cannot be empty
     */
    public NacosDataSource(final String serverAddr, final String groupId, final String dataId, SentinelConverter<String, T> parser, final DataSourceMode dataSourceMode) {
        this(NacosDataSource.buildProperties(serverAddr), groupId, dataId, parser, dataSourceMode);
    }

    /**
     *
     * @param properties properties for construct {@link ConfigService} using {@link NacosFactory#createConfigService(Properties)}
     * @param groupId    group ID, cannot be empty
     * @param dataId     data ID, cannot be empty
     * @param parser     customized data parser, cannot be empty
     */
    public NacosDataSource(final Properties properties, final String groupId, final String dataId, SentinelConverter<String, T> parser) {
        this(properties, groupId, dataId, parser, DataSourceMode.READABLE);
    }

    /**
     *
     * @param properties properties for construct {@link ConfigService} using {@link NacosFactory#createConfigService(Properties)}
     * @param groupId    group ID, cannot be empty
     * @param dataId     data ID, cannot be empty
     * @param parser     customized data parser, cannot be empty
     * @param dataSourceMode     dataSourceMode, cannot be empty
     */
    public NacosDataSource(final Properties properties, final String groupId, final String dataId, SentinelConverter<String, T> parser, final DataSourceMode dataSourceMode) {
        super(parser, dataSourceMode);

        if (StringUtil.isBlank(groupId) || StringUtil.isBlank(dataId)) {
            throw new IllegalArgumentException(String.format("Bad argument: groupId=[%s], dataId=[%s]", groupId, dataId));
        }
        AssertUtil.notNull(properties, "Nacos properties must not be null, you could put some keys from PropertyKeyConst");

        try {
            super.setDataSourceClient(NacosFactory.createConfigService(properties));
        } catch (NacosException ex) {
            RecordLog.warn("[NacosDataSource] Error when create configService which is NacosClient", ex);
        }

        if(DataSourceMode.ALL == dataSourceMode || DataSourceMode.READABLE == dataSourceMode) {
            this.readableDataSource = new NacosReadableDataSource(properties, groupId, dataId, this);
        }

        if(DataSourceMode.ALL == dataSourceMode || DataSourceMode.WRITABLE == dataSourceMode) {
            this.writableDataSource = new NacosWritableDataSource(properties, groupId, dataId, this);
        }

    }

    private static Properties buildProperties(String serverAddr) {
        Properties properties = new Properties();
        properties.setProperty(PropertyKeyConst.SERVER_ADDR, serverAddr);
        return properties;
    }

    public NacosReadableDataSource<T> getReader() {
        return this.readableDataSource;
    }

    public NacosWritableDataSource<T> getWriter() {
        return this.writableDataSource;
    }

    public void close() {
        if(DataSourceMode.ALL == dataSourceMode || DataSourceMode.READABLE == dataSourceMode) {
            this.readableDataSource.close();
        }

        if(DataSourceMode.ALL == dataSourceMode || DataSourceMode.WRITABLE == dataSourceMode) {
            this.writableDataSource.close();
        }
    }

}
