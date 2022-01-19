package com.alibaba.csp.sentinel.datasource.nacos;

import com.alibaba.csp.sentinel.datasource.AbstractWritableDataSource;
import com.alibaba.nacos.api.config.ConfigService;

/**
 * A writable {@code DataSource} with Nacos backend. Which can push the new value so to the nacos server.
 *
 * @author Jiajiangnan
 */
public class NacosWritableDataSource<T> extends AbstractWritableDataSource<String, T> {

    private final ConfigService configService;
    private final String groupId;
    private final String dataId;

    public NacosWritableDataSource(NacosDataSourceContext<T> context) {
        super(context);

        this.configService = (ConfigService)context.getClient();
        this.groupId = context.getGroupId();
        this.dataId = context.getDataId();
    }

    @Override
    public void write(T value) throws Exception {
        String fromSentinel = context.getConverter().fromSentinel(value);
        configService.publishConfig(dataId, groupId, fromSentinel);
    }

    @Override
    public void close() throws Exception {
        // Nothing to do
    }

}
