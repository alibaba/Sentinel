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
package com.alibaba.csp.sentinel.datasource.consul;

import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.alibaba.csp.sentinel.datasource.AbstractDataSource;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.AssertUtil;

import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetValue;

import java.util.concurrent.*;

/**
 * <p>
 * A read-only {@code DataSource} with Consul backend.
 * <p>
 * <p>
 * The data source first initial rules from a Consul during initialization.
 * Then it start a watcher to observe the updates of rule date and update to memory.
 *
 * Consul do not provide http api to watch the update of KV，so it use a long polling and
 * <a href="https://www.consul.io/api/features/blocking.html">blocking queries</a> of the Consul's feature
 * to watch and update value easily.When Querying data by index will blocking until change or timeout. If
 * the index of the current query is larger than before, it means that the data has changed.
 * </p>
 *
 * @author wavesZh
 */
public class ConsulDataSource<T> extends AbstractDataSource<String, T> {

    private static final int DEFAULT_PORT = 8500;

    private final String address;
    private final String ruleKey;
    /**
     * Request of query will hang until timeout (in second) or get updated value.
     */
    private final int watchTimeout;

    /**
     * Record the data's index in Consul to watch the change.
     * If lastIndex is smaller than the index of next query, it means that rule data has updated.
     */
    private volatile long lastIndex;

    private final ConsulClient client;

    private final ConsulKVWatcher watcher = new ConsulKVWatcher();

    @SuppressWarnings("PMD.ThreadPoolCreationRule")
    private final ExecutorService watcherService = Executors.newSingleThreadExecutor(
        new NamedThreadFactory("sentinel-consul-ds-watcher", true));

    public ConsulDataSource(String host, String ruleKey, int watchTimeoutInSecond, Converter<String, T> parser) {
        this(host, DEFAULT_PORT, ruleKey, watchTimeoutInSecond, parser);
    }

    /**
     * Constructor of {@code ConsulDataSource}.
     *
     * @param parser       customized data parser, cannot be empty
     * @param host         consul agent host
     * @param port         consul agent port
     * @param ruleKey      data key in Consul
     * @param watchTimeout request for querying data will be blocked until new data or timeout. The unit is second (s)
     */
    public ConsulDataSource(String host, int port, String ruleKey, int watchTimeout, Converter<String, T> parser) {
        super(parser);
        AssertUtil.notNull(host, "Consul host can not be null");
        AssertUtil.notEmpty(ruleKey, "Consul ruleKey can not be empty");
        AssertUtil.isTrue(watchTimeout >= 0, "watchTimeout should not be negative");
        this.client = new ConsulClient(host, port);
        this.address = host + ":" + port;
        this.ruleKey = ruleKey;
        this.watchTimeout = watchTimeout;
        loadInitialConfig();
        startKVWatcher();
    }

    private void startKVWatcher() {
        watcherService.submit(watcher);
    }

    private void loadInitialConfig() {
        try {
            T newValue = loadConfig();
            if (newValue == null) {
                RecordLog.warn(
                    "[ConsulDataSource] WARN: initial config is null, you may have to check your data source");
            }
            getProperty().updateValue(newValue);
        } catch (Exception ex) {
            RecordLog.warn("[ConsulDataSource] Error when loading initial config", ex);
        }
    }

    @Override
    public String readSource() throws Exception {
        if (this.client == null) {
            throw new IllegalStateException("Consul has not been initialized or error occurred");
        }
        Response<GetValue> response = getValueImmediately(ruleKey);
        if (response != null) {
            GetValue value = response.getValue();
            lastIndex = response.getConsulIndex();
            return value != null ? value.getDecodedValue() : null;
        }
        return null;
    }

    @Override
    public void close() throws Exception {
        watcher.stop();
        watcherService.shutdown();
    }

    private class ConsulKVWatcher implements Runnable {
        private boolean running = true;

        @Override
        public void run() {
            while (running) {
                // It will be blocked until watchTimeout(s) if rule data has no update.
                Response<GetValue> response = getValue(ruleKey, lastIndex, watchTimeout);
                if (response == null) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(watchTimeout * 1000);
                    } catch (InterruptedException e) {
                    }
                    continue;
                }
                GetValue getValue = response.getValue();
                Long currentIndex = response.getConsulIndex();
                if (currentIndex == null || currentIndex <= lastIndex) {
                    continue;
                }
                lastIndex = currentIndex;
                if (getValue != null) {
                    String newValue = getValue.getDecodedValue();
                    try {
                        getProperty().updateValue(parser.convert(newValue));
                        RecordLog.info("[ConsulDataSource] New property value received for ({0}, {1}): {2}",
                            address, ruleKey, newValue);
                    } catch (Exception ex) {
                        // In case of parsing error.
                        RecordLog.warn("[ConsulDataSource] Failed to update value for ({0}, {1}), raw value: {2}",
                            address, ruleKey, newValue);
                    }
                }
            }
        }

        private void stop() {
            running = false;
        }
    }

    /**
     * Get data from Consul immediately.
     *
     * @param key data key in Consul
     * @return the value associated to the key, or null if error occurs
     */
    private Response<GetValue> getValueImmediately(String key) {
        return getValue(key, -1, -1);
    }

    /**
     * Get data from Consul (blocking).
     *
     * @param key      data key in Consul
     * @param index    the index of data in Consul.
     * @param waitTime time(second) for waiting get updated value.
     * @return the value associated to the key, or null if error occurs
     */
    private Response<GetValue> getValue(String key, long index, long waitTime) {
        try {
            return client.getKVValue(key, new QueryParams(waitTime, index));
        } catch (Throwable t) {
            RecordLog.warn("[ConsulDataSource] Failed to get value for key: " + key, t);
        }
        return null;
    }

}
