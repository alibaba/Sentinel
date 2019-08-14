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

import static java.util.concurrent.Executors.newSingleThreadExecutor;

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

    private final ConsulClient client;

    private final String address;

    private String ruleKey;
    /**
     * Record the data's index in Consul to watch the change.
     * If lastIndex is smaller than the index of next query, it means that rule data has updated.
     */
    private volatile long lastIndex;
    /**
     * Request of query will hang until timeout(ms) or get updated value.
     */
    private int watchTimeout;

    private ConsulKVWatcher watcher = new ConsulKVWatcher();

    private ExecutorService watcherService = newSingleThreadExecutor(
            new NamedThreadFactory("sentinel-consul-ds-update", true));

    public ConsulDataSource(Converter<String, T> parser, String host, String ruleKey, int watchTimeout) {
        this(host, DEFAULT_PORT, ruleKey, watchTimeout, parser);
    }

    /**
     * Constructor of {@code ConsulDataSource}.
     * @param parser          customized data parser, cannot be empty
     * @param host            consul agent host
     * @param port            consul agent port
     * @param ruleKey         data key in Consul
     * @param watchTimeout    request for querying data will be blocked until new data or timeout. The unit is ms
     */
    public ConsulDataSource(String host, int port, String ruleKey, int watchTimeout, Converter<String, T> parser) {
        super(parser);
        AssertUtil.notNull(host, "Consul host can not be null");
        AssertUtil.notEmpty(ruleKey, "Consul ruleKey can not be empty");
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
                // It will be blocked until watchTimeout(ms) if rule data has no update.
                Response<GetValue> response = getValue(ruleKey, lastIndex, watchTimeout / 1000);
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

    /**
     * get data from Consul immediately.
     *
     * @param key data key in Consul
     */
    public Response<GetValue> getValueImmediately(String key) {
        return getValue(key, -1, -1);
    }
    /**
     * get data from Consul.
     *
     * @param key data key in Consul
     * @param index the index of data in Consul.
     * @param waitTime time(second) for waiting get updated value.
     */
    private Response<GetValue> getValue(String key, long index, long waitTime) {
        try {
            return client.getKVValue(key, new QueryParams(waitTime, index));
        } catch (Throwable t) {
            RecordLog.warn("fail to get value for key: " + key);
        }
        return null;
    }

}
