package com.alibaba.csp.sentinel.datasource.zookeeper;

import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.alibaba.csp.sentinel.datasource.AbstractDataSource;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.google.common.collect.Maps;
import org.apache.curator.framework.AuthInfo;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * A read-only {@code DataSource} with ZooKeeper backend.
 *
 * @author guonanjun
 */
public class ZookeeperDataSource<T> implements ReadableDataSource<String, T> {
    private static final int RETRY_TIMES = 3;
    private static final int SLEEP_TIME = 1000;
    private static final int THREAD_COUNT = 1;
    private static final int POOL_QUEUE_SIZE = 10;

    private static ExecutorService pool = null;
    private static CuratorFramework zkClient = null;
    private static final AtomicInteger POOL_REFERENCE_COUNT = new AtomicInteger(0);

    /**
     * just for compatible begin: deprecated usage that new many ZookeeperDataSource instances
     */
    private final AbstractDataSource<String, T> dataSource;
    private final String path;
    // just for compatible end

    private final Map<String, AbstractDataSource> pathDataSourceMap = Maps.newConcurrentMap();
    private final String serverAddr;

    @Deprecated
    public ZookeeperDataSource(final String serverAddr, final String path, Converter<String, T> parser) {
        if (StringUtil.isBlank(serverAddr) || StringUtil.isBlank(path)) {
            throw new IllegalArgumentException(String.format("Bad argument: serverAddr=[%s], path=[%s]", serverAddr, path));
        }
        this.path = path;
        this.serverAddr = serverAddr;

        initPoolAndClient(serverAddr, null);

        this.dataSource = new ZookeeperDataSourceInner<>(path, parser);

        loadInitialConfig(this.dataSource);
    }

    /**
     * This constructor is Nacos-style.
     */
    @Deprecated
    public ZookeeperDataSource(final String serverAddr, final String groupId, final String dataId,
                               Converter<String, T> parser) {
        if (StringUtil.isBlank(serverAddr) || StringUtil.isBlank(groupId) || StringUtil.isBlank(dataId)) {
            throw new IllegalArgumentException(String.format("Bad argument: serverAddr=[%s], groupId=[%s], dataId=[%s]", serverAddr, groupId, dataId));
        }
        this.path = getPath(groupId, dataId);
        this.serverAddr = serverAddr;

        initPoolAndClient(serverAddr, null);

        this.dataSource = new ZookeeperDataSourceInner<>(path, parser);

        loadInitialConfig(this.dataSource);
    }

    /**
     * This constructor adds authentication information.
     */
    @Deprecated
    public ZookeeperDataSource(final String serverAddr, final List<AuthInfo> authInfos, final String groupId, final String dataId,
                               Converter<String, T> parser) {
        if (StringUtil.isBlank(serverAddr) || StringUtil.isBlank(groupId) || StringUtil.isBlank(dataId)) {
            throw new IllegalArgumentException(String.format("Bad argument: serverAddr=[%s], authInfos=[%s], groupId=[%s], dataId=[%s]", serverAddr, authInfos, groupId, dataId));
        }
        this.path = getPath(groupId, dataId);
        this.serverAddr = serverAddr;

        initPoolAndClient(serverAddr, authInfos);

        this.dataSource = new ZookeeperDataSourceInner<>(path, parser);

        loadInitialConfig(this.dataSource);
    }

    public ZookeeperDataSource(final String serverAddr) {
        this(serverAddr, null);
    }

    public ZookeeperDataSource(final String serverAddr, final List<AuthInfo> authInfos) {
        if (StringUtil.isBlank(serverAddr)) {
            throw new IllegalArgumentException(String.format("Bad argument: serverAddr=[%s]", serverAddr));
        }
        path = null;
        this.serverAddr = serverAddr;

        initPoolAndClient(serverAddr, authInfos);

        this.dataSource = null;
    }

    public <W> ZookeeperDataSource register(final String path, Converter<String, W> parser) throws Exception {
        final AbstractDataSource<String, W> dataSource = new ZookeeperDataSourceInner<>(path, parser);

        loadInitialConfig(dataSource);

        pathDataSourceMap.put(path, dataSource);

        return this;
    }

    public <W> ZookeeperDataSource register(final String groupId, final String dataId, Converter<String, W> parser) throws Exception {
        return register(getPath(groupId, dataId), parser);
    }

    private <P> void loadInitialConfig(AbstractDataSource<String, P> dataSource) {
        try {
            P newValue = dataSource.loadConfig();
            if (newValue == null) {
                RecordLog.warn("[ZookeeperDataSource] WARN: initial config is null, you may have to check your data source");
            }
            dataSource.getProperty().updateValue(newValue);
        } catch (Exception ex) {
            RecordLog.warn("[ZookeeperDataSource] Error when loading initial config", ex);
        }
    }

    private synchronized void initPoolAndClient(final String serverAddr, final List<AuthInfo> authInfos) {
        if (pool == null) {
            pool = new ThreadPoolExecutor(THREAD_COUNT, THREAD_COUNT, 0, TimeUnit.MILLISECONDS,
                    new ArrayBlockingQueue<Runnable>(POOL_QUEUE_SIZE), new NamedThreadFactory("sentinel-zookeeper-ds-update"),
                    new ThreadPoolExecutor.DiscardOldestPolicy());
        }

        if (zkClient == null) {
            if (authInfos == null || authInfos.size() == 0) {
                zkClient = CuratorFrameworkFactory.newClient(serverAddr, new ExponentialBackoffRetry(SLEEP_TIME, RETRY_TIMES));
            } else {
                zkClient = CuratorFrameworkFactory.builder().
                        connectString(serverAddr).
                        retryPolicy(new ExponentialBackoffRetry(SLEEP_TIME, RETRY_TIMES)).
                        authorization(authInfos).
                        build();
            }
            zkClient.start();
        }
    }

    public static String getPath(String groupId, String dataId) {
        return String.format("/%s/%s", groupId, dataId);
    }

    @Override
    public synchronized void close() throws Exception {
        if (POOL_REFERENCE_COUNT.decrementAndGet() == 0 && pool != null) {
            pool.shutdown();
            pool = null;
        }

        for (AbstractDataSource dataSource : pathDataSourceMap.values()) {
            if (dataSource != null) {
                dataSource.close();
            }
        }

        if (zkClient != null) {
            zkClient.close();
            zkClient = null;
        }
    }

    public SentinelProperty<T> getProperty(String path) {
        AbstractDataSource dataSource = this.pathDataSourceMap.get(path);
        if (dataSource == null) {
            String errorMsg = String.format("the datasource for this path isn't exist, path=[%s], pathDataSourceMap.keySet=[%s]", path, pathDataSourceMap.keySet());
            RecordLog.warn(errorMsg);
            throw new IllegalStateException(errorMsg);
        }

        return dataSource.getProperty();
    }

    @Deprecated
    @Override
    public T loadConfig() throws Exception {
        return null;
    }

    @Deprecated
    @Override
    public String readSource() throws Exception {
        return null;
    }

    @Deprecated
    @Override
    public SentinelProperty getProperty() {
        return dataSource != null ? dataSource.getProperty() : null;
    }

    private class ZookeeperDataSourceInner<T> extends AbstractDataSource<String, T> {
        private NodeCache nodeCache = null;
        private NodeCacheListener listener;

        public ZookeeperDataSourceInner(final String path, Converter<String, T> parser) {
            super(parser);

            try {
                this.listener = new NodeCacheListener() {
                    @Override
                    public void nodeChanged() {

                        try {
                            T newValue = loadConfig();
                            RecordLog.info(String.format("[ZookeeperDataSource] New property value received for (%s, %s): %s",
                                    serverAddr, path, newValue));
                            // Update the new value to the property.
                            getProperty().updateValue(newValue);
                        } catch (Exception ex) {
                            RecordLog.warn("[ZookeeperDataSource] loadConfig exception", ex);
                        }
                    }
                };
                this.nodeCache = new NodeCache(zkClient, path);
                this.nodeCache.getListenable().addListener(listener, pool);
                this.nodeCache.start();
                POOL_REFERENCE_COUNT.incrementAndGet();
            } catch (Exception e) {
                RecordLog.warn("[ZookeeperDataSource] Error when start NodeCache", e);
            }
        }

        @Override
        public String readSource() throws Exception {
            if (zkClient == null) {
                throw new IllegalStateException("Zookeeper has not been initialized or error occurred");
            }
            String configInfo = null;
            ChildData childData = this.nodeCache.getCurrentData();
            if (null != childData && childData.getData() != null) {

                configInfo = new String(childData.getData());
            }
            return configInfo;
        }

        @Override
        public void close() throws Exception {
            if (this.nodeCache != null) {
                this.nodeCache.getListenable().removeListener(this.listener);
                this.nodeCache.close();
            }
        }
    }
}
