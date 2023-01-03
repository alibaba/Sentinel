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

import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.alibaba.csp.sentinel.datasource.AbstractReadableDataSource;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.nacos.api.config.ConfigService;
import com.alibaba.nacos.api.config.listener.Listener;

import java.util.Properties;
import java.util.concurrent.*;

/**
 * A read-only {@code DataSource} with Nacos backend. When the data in Nacos backend has been modified,
 * Nacos will automatically push the new value so that the dynamic configuration can be real-time.
 *
 * @author Eric Zhao
 * @author Jiajiangnan
 */
public class NacosReadableDataSource<T> extends AbstractReadableDataSource<String, T> {

    private final ConfigService configService;
    private final Properties properties;
    private final String groupId;
    private final String dataId;

    private static final int DEFAULT_TIMEOUT = 3000;
    /**
     * Single-thread pool. Once the thread pool is blocked, we throw up the old task.
     */
    private final ExecutorService pool = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<Runnable>(1), new NamedThreadFactory("sentinel-nacos-ds-update", true),
            new ThreadPoolExecutor.DiscardOldestPolicy());

    private final Listener configListener;

    public NacosReadableDataSource(NacosDataSourceContext<T> context) {
        super(context);
        this.configService = (ConfigService)context.getClient();
        this.properties = context.getProperties();
        this.groupId = context.getGroupId();
        this.dataId = context.getDataId();

        if (StringUtil.isBlank(groupId) || StringUtil.isBlank(dataId)) {
            throw new IllegalArgumentException(String.format("Bad argument: groupId=[%s], dataId=[%s]", groupId, dataId));
        }
        this.configListener = new Listener() {
            @Override
            public Executor getExecutor() {
                return pool;
            }

            @Override
            public void receiveConfigInfo(final String configInfo) {
                RecordLog.info("[NacosDataSource] New property value received for (properties: {}) (dataId: {}, groupId: {}): {}", properties, dataId, groupId, configInfo);
                T newValue = context.getConverter().toSentinel(configInfo);
                // Update the new value to the property.
                getProperty().updateValue(newValue);
            }
        };

        initNacosListener();
        loadInitialConfig();
    }

    private void loadInitialConfig() {
        try {
            T newValue = loadConfig();
            if (newValue == null) {
                RecordLog.warn("[NacosDataSource] WARN: initial config is null, you may have to check your data source");
            }
            getProperty().updateValue(newValue);
        } catch (Exception ex) {
            RecordLog.warn("[NacosDataSource] Error when loading initial config", ex);
        }
    }

    private void initNacosListener() {
        try {
            // Add config listener.
            configService.addListener(dataId, groupId, configListener);
        } catch (Exception e) {
            RecordLog.warn("[NacosDataSource] Error occurred when initializing Nacos data source", e);
            e.printStackTrace();
        }
    }

    @Override
    public String readSource() throws Exception {
        if (configService == null) {
            throw new IllegalStateException("Nacos config service has not been initialized or error occurred");
        }
        return configService.getConfig(dataId, groupId, DEFAULT_TIMEOUT);
    }

    @Override
    public void close() throws Exception {
        if (configService != null) {
            configService.removeListener(dataId, groupId, configListener);
        }
        pool.shutdownNow();
    }

}
