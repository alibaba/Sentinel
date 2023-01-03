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
package com.alibaba.csp.sentinel.datasource.nacos;

import com.alibaba.csp.sentinel.datasource.AbstractDataSourceContext;
import com.alibaba.csp.sentinel.datasource.DataSoureMode;
import com.alibaba.csp.sentinel.datasource.converter.SentinelConverter;

import java.util.Properties;

/**
 * Nacos context through DataSource which hold some common variables.
 *
 * @author Jiajiangnan
 */
public class NacosDataSourceContext<T> extends AbstractDataSourceContext<String, T> {

    private final Properties properties;
    private final String groupId;
    private final String dataId;

    public NacosDataSourceContext(Object client, Properties properties, String groupId, String dataId, SentinelConverter<String, T> converter, DataSoureMode dataSoureMode) {
        super(client, converter, dataSoureMode);
        this.properties = properties;
        this.groupId = groupId;
        this.dataId = dataId;
    }

    public Properties getProperties() {
        return properties;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getDataId() {
        return dataId;
    }

}
