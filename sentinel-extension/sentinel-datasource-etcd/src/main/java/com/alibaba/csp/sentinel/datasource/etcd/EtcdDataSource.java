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
package com.alibaba.csp.sentinel.datasource.etcd;

import com.alibaba.csp.sentinel.datasource.AbstractDataSource;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.log.RecordLog;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import io.etcd.jetcd.KeyValue;
import io.etcd.jetcd.Watch;
import io.etcd.jetcd.kv.GetResponse;
import io.etcd.jetcd.watch.WatchEvent;

import java.nio.charset.Charset;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * A read-only {@code DataSource} with Etcd backend. When the data in Etcd backend has been modified,
 * Etcd will automatically push the new value so that the dynamic configuration can be real-time.
 *
 * @author lianglin
 * @since 1.7.0
 */
public class EtcdDataSource<T> extends AbstractDataSource<String, T> {

    private final Client client;
    private Watch.Watcher watcher;

    private final String key;
    private Charset charset = Charset.forName(EtcdConfig.getCharset());

    /**
     * Create an etcd data-source. The connection configuration will be retrieved from {@link EtcdConfig}.
     *
     * @param key    config key
     * @param parser data parser
     */
    public EtcdDataSource(String key, Converter<String, T> parser) {
        super(parser);
        if (!EtcdConfig.isAuthEnable()) {
            this.client = Client.builder()
                .endpoints(EtcdConfig.getEndPoints().split(",")).build();
        } else {
            this.client = Client.builder()
                .endpoints(EtcdConfig.getEndPoints().split(","))
                .user(ByteSequence.from(EtcdConfig.getUser(), charset))
                .password(ByteSequence.from(EtcdConfig.getPassword(), charset))
                .authority(EtcdConfig.getAuthority())
                .build();
        }
        this.key = key;
        loadInitialConfig();
        initWatcher();
    }

    private void loadInitialConfig() {
        try {
            T newValue = loadConfig();
            if (newValue == null) {
                RecordLog.warn(
                    "[EtcdDataSource] Initial configuration is null, you may have to check your data source");
            }
            getProperty().updateValue(newValue);
        } catch (Exception ex) {
            RecordLog.warn("[EtcdDataSource] Error when loading initial configuration", ex);
        }
    }

    private void initWatcher() {
        watcher = client.getWatchClient().watch(ByteSequence.from(key, charset), (watchResponse) -> {
            for (WatchEvent watchEvent : watchResponse.getEvents()) {
                WatchEvent.EventType eventType = watchEvent.getEventType();
                if (eventType == WatchEvent.EventType.PUT) {
                    try {
                        String newValueJson = watchEvent.getKeyValue().getValue().toString(charset);
                        T newValue = parser.convert(newValueJson);
                        getProperty().updateValue(newValue);
                    } catch (Exception e) {
                        RecordLog.warn("[EtcdDataSource] Failed to update config", e);
                    }
                } else if (eventType == WatchEvent.EventType.DELETE) {
                    RecordLog.info("[EtcdDataSource] Cleaning config for key <{}>", key);
                    getProperty().updateValue(null);
                }
            }
        });
    }

    @Override
    public String readSource() throws Exception {
        CompletableFuture<GetResponse> responseFuture = client.getKVClient().get(ByteSequence.from(key, charset));
        List<KeyValue> kvs = responseFuture.get().getKvs();
        return kvs.size() == 0 ? null : kvs.get(0).getValue().toString(charset);
    }

    @Override
    public void close() {
        if (watcher != null) {
            try {
                watcher.close();
            } catch (Exception ex) {
                RecordLog.info("[EtcdDataSource] Failed to close watcher", ex);
            }
        }
        if (client != null) {
            client.close();
        }
    }
}
