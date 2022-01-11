package com.alibaba.csp.sentinel.datasource.nacos;

import com.alibaba.csp.sentinel.datasource.AbstractDataSourceContext;
import com.alibaba.csp.sentinel.datasource.DataSoureMode;
import com.alibaba.csp.sentinel.datasource.converter.SentinelConverter;

import java.util.Properties;

/**
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
