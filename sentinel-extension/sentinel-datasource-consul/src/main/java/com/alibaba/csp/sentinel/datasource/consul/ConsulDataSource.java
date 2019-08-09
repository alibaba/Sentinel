package com.alibaba.csp.sentinel.datasource.consul;

import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.alibaba.csp.sentinel.datasource.AbstractDataSource;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.ecwid.consul.v1.ConsulClient;
import com.ecwid.consul.v1.QueryParams;
import com.ecwid.consul.v1.Response;
import com.ecwid.consul.v1.kv.model.GetValue;

import java.util.concurrent.*;

import static java.util.concurrent.Executors.newSingleThreadExecutor;

/**
 * A read-only {@code DataSource} with Consul backend.
 *
 */
public class ConsulDataSource<T> extends AbstractDataSource<String, T> {

    private final ConsulClient client;

    private final String address;

    private String ruleKey;

    private volatile long lastIndex;

    private int watchTimeout;

    private ConsulKVWatcher watcher = new ConsulKVWatcher();

    private ExecutorService watcherService = newSingleThreadExecutor(
            new NamedThreadFactory("sentinel-consul-ds-update", true));

    public ConsulDataSource(Converter<String, T> parser, String host, int port, String ruleKey, int watchTimeout) {
        super(parser);
        this.client = new ConsulClient(host, port);
        address = host + ":" + port;
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
                RecordLog.warn("[ConsulDataSource] WARN: initial config is null, you may have to check your data source");
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
        Response<GetValue> response = getValue(ruleKey);
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
                Response<GetValue> response = getValue(ruleKey);
                if (response == null) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(watchTimeout);
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
                    getProperty().updateValue(parser.convert(newValue));
                    RecordLog.info(String.format("[ConsulDataSource] New property value received for (%s, %s): %s",
                            address, ruleKey, newValue));
                }
            }
        }

        private void stop() {
            running = false;
        }
    }

    private Response<GetValue> getValue(String key) {
        try {
            return client.getKVValue(key, new QueryParams(watchTimeout, lastIndex));
        } catch (Throwable t) {
            RecordLog.warn("fail to get value for key: " + key);
        }
        return null;
    }

}
