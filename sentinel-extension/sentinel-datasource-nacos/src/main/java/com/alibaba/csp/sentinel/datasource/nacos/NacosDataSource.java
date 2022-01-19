package com.alibaba.csp.sentinel.datasource.nacos;

import com.alibaba.csp.sentinel.datasource.*;
import com.alibaba.csp.sentinel.datasource.converter.SentinelConverter;
import com.alibaba.csp.sentinel.util.AssertUtil;
import com.alibaba.nacos.api.NacosFactory;
import com.alibaba.nacos.api.PropertyKeyConst;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.exception.NacosException;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 *
 * @author Eric Zhao
 * @author Jiajiangnan
 */
public class NacosDataSource<T> extends SentinelDataSource {

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(NacosDataSource.class);

    private ReadableDataSource reader;
    private WritableDataSource writer;
    private final NacosDataSourceContext<T> context;

    /**
     * @deprecated since 1.8.4.
     * Only for compatible with older versions < 1.8.4-SNAPSHOT
     * Constructs an read-only DataSource with Nacos backend.
     *
     * @param serverAddr server address of Nacos, cannot be empty
     * @param groupId    group ID, cannot be empty
     * @param dataId     data ID, cannot be empty
     * @param converter     customized data parser, cannot be empty
     */
    @Deprecated
    public NacosDataSource(final String serverAddr, final String groupId, final String dataId, final Converter<String, T> converter) {
        this(serverAddr, groupId, dataId, new SentinelConverter<String, T>() {
            @Override
            public T toSentinel(String source) {
                return converter.convert(source);
            }
        });
    }

    /**
     * @deprecated since 1.8.4.
     * Only for compatible with older versions < 1.8.4-SNAPSHOT
     *
     * @param properties properties for construct {@link ConfigService} using {@link NacosFactory#createConfigService(Properties)}
     * @param groupId    group ID, cannot be empty
     * @param dataId     data ID, cannot be empty
     * @param converter     customized data parser, cannot be empty
     */
    @Deprecated
    public NacosDataSource(final Properties properties, final String groupId, final String dataId, final Converter<String, T> converter) {
        this(properties, groupId, dataId, new SentinelConverter<String, T>() {
            @Override
            public T toSentinel(String source) {
                return converter.convert(source);
            }
        });
    }

    /**
     * Constructs an read-only DataSource with Nacos backend.
     *
     * @param serverAddr server address of Nacos, cannot be empty
     * @param groupId    group ID, cannot be empty
     * @param dataId     data ID, cannot be empty
     * @param converter     customized data parser, cannot be empty
     */
    public NacosDataSource(final String serverAddr, final String groupId, final String dataId, final SentinelConverter<String, T> converter) {
        this(serverAddr, groupId, dataId, converter, DataSoureMode.READABLE);
    }

    public NacosDataSource(final String serverAddr, final String groupId, final String dataId, final SentinelConverter<String, T> converter, final DataSoureMode dataSoureMode) {
        this(NacosDataSource.buildProperties(serverAddr), groupId, dataId, converter, dataSoureMode);
    }

    /**
     *
     * @param properties properties for construct {@link ConfigService} using {@link NacosFactory#createConfigService(Properties)}
     * @param groupId    group ID, cannot be empty
     * @param dataId     data ID, cannot be empty
     * @param converter     customized data parser, cannot be empty
     */
    public NacosDataSource(final Properties properties, final String groupId, final String dataId, final SentinelConverter<String, T> converter) {
        this(properties, groupId, dataId, converter, DataSoureMode.READABLE);
    }

    /**
     *
     * @param properties properties for construct {@link ConfigService} using {@link NacosFactory#createConfigService(Properties)}
     * @param groupId    group ID, cannot be empty
     * @param dataId     data ID, cannot be empty
     * @param converter  customized data parser, cannot be empty
     * @param dataSoureMode  datasource mode, for example: readable,writable, all
     */
    public NacosDataSource(final Properties properties, final String groupId, final String dataId, final SentinelConverter<String, T> converter, final DataSoureMode dataSoureMode) {
        // init DataSourceClient
        ConfigService dataSourceClient = getDataSourceClient(properties);
        // init DataSourceContext
        this.context = new NacosDataSourceContext(dataSourceClient, properties, groupId, dataId, converter, dataSoureMode);
        // init DataSource
        initDataSource();
    }

    private ConfigService getDataSourceClient(Properties properties) {
        try {
            AssertUtil.notNull(properties, "Nacos properties must not be null, you could put some keys from PropertyKeyConst");
            return NacosFactory.createConfigService(properties);
        } catch (NacosException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void initDataSource() {
        ConfigService dataSourceClient = context.getClient() == null ? null : (ConfigService) context.getClient();
        if(dataSourceClient == null) {
            throw new IllegalStateException("Nacos config service has not been initialized or error occurred");
        }
        DataSoureMode dataSoureMode = context.getDataSourceMode();
        if(DataSoureMode.ALL == dataSoureMode || DataSoureMode.READABLE == dataSoureMode) {
            reader = new NacosReadableDataSource(this.context);
        }

        if(DataSoureMode.ALL == dataSoureMode || DataSoureMode.WRITABLE == dataSoureMode) {
            writer = new NacosWritableDataSource(this.context);
        }
    }

    @Override
    public ReadableDataSource getReader() {
        return this.reader;
    }

    @Override
    public WritableDataSource getWriter() {
        return this.writer;
    }

    @Override
    protected void postDataSourceClose() {
        ConfigService dataSourceClient = context.getClient() == null ? null : (ConfigService) context.getClient();
        if(dataSourceClient != null) {
            try {
                dataSourceClient.shutDown();
            } catch (NacosException e) {
                logger.error(e.getErrMsg(), e);
            }
        }
    }

    private static Properties buildProperties(String serverAddr) {
        Properties properties = new Properties();
        properties.setProperty(PropertyKeyConst.SERVER_ADDR, serverAddr);
        return properties;
    }

}
