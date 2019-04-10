package com.alibaba.csp.sentinel.datasource.zookeeper;

import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.alibaba.csp.sentinel.datasource.AbstractDataSource;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRule;
import com.alibaba.csp.sentinel.slots.block.authority.AuthorityRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.apache.curator.framework.AuthInfo;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 *
 * A read-only {@code DataSource} with ZooKeeper backend.
 *
 * @author guonanjun
 */
public class ZookeeperDataSource<T> extends AbstractDataSource<String, T> {
    private static final int RETRY_TIMES = 3;
    private static final int SLEEP_TIME = 1000;

    private static ExecutorService pool = null;
    private static final AtomicInteger poolReferenceCount = new AtomicInteger(0);

    private NodeCacheListener listener;
    private final String path;

    private static CuratorFramework zkClient = null;
    private NodeCache nodeCache = null;

    @Deprecated
    public ZookeeperDataSource(final String serverAddr, final String path, Converter<String, T> parser) {
        super(parser);
        if (StringUtil.isBlank(serverAddr) || StringUtil.isBlank(path)) {
            throw new IllegalArgumentException(String.format("Bad argument: serverAddr=[%s], path=[%s]", serverAddr, path));
        }
        this.path = path;

        init(serverAddr, null);
    }

    /**
     * This constructor is Nacos-style.
     */
    @Deprecated
    public ZookeeperDataSource(final String serverAddr, final String groupId, final String dataId,
                               Converter<String, T> parser) {
        super(parser);
        if (StringUtil.isBlank(serverAddr) || StringUtil.isBlank(groupId) || StringUtil.isBlank(dataId)) {
            throw new IllegalArgumentException(String.format("Bad argument: serverAddr=[%s], groupId=[%s], dataId=[%s]", serverAddr, groupId, dataId));
        }
        this.path = getPath(groupId, dataId);

        init(serverAddr, null);
    }

    /**
     * This constructor adds authentication information.
     */
    @Deprecated
    public ZookeeperDataSource(final String serverAddr, final List<AuthInfo> authInfos, final String groupId, final String dataId,
                               Converter<String, T> parser) {
        super(parser);
        if (StringUtil.isBlank(serverAddr) || StringUtil.isBlank(groupId) || StringUtil.isBlank(dataId)) {
            throw new IllegalArgumentException(String.format("Bad argument: serverAddr=[%s], authInfos=[%s], groupId=[%s], dataId=[%s]", serverAddr, authInfos, groupId, dataId));
        }
        this.path = getPath(groupId, dataId);

        init(serverAddr, authInfos);
    }

    private void init(final String serverAddr, final List<AuthInfo> authInfos) {
        initZookeeperListener(serverAddr, authInfos);
        loadInitialConfig();
    }

    private void loadInitialConfig() {
        try {
            T newValue = loadConfig();
            if (newValue == null) {
                RecordLog.warn("[ZookeeperDataSource] WARN: initial config is null, you may have to check your data source");
            }
            getProperty().updateValue(newValue);
        } catch (Exception ex) {
            RecordLog.warn("[ZookeeperDataSource] Error when loading initial config", ex);
        }
    }

    private void initZookeeperListener(final String serverAddr, final List<AuthInfo> authInfos) {
        try {
            if (pool == null) {
                synchronized (ZookeeperDataSource.class) {
                    if (pool == null) {
                        pool = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS,
                                new ArrayBlockingQueue<Runnable>(1), new NamedThreadFactory("sentinel-zookeeper-ds-update"),
                                new ThreadPoolExecutor.DiscardOldestPolicy());
                    }
                }
            }

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

            if (zkClient == null) {
                synchronized (ZookeeperDataSource.class) {
                    if (zkClient == null) {
                        initClient(serverAddr, authInfos);
                    }
                }
            }

            this.nodeCache = new NodeCache(zkClient, this.path);
            this.nodeCache.getListenable().addListener(this.listener, pool);
            this.nodeCache.start();
            poolReferenceCount.incrementAndGet();
        } catch (Exception e) {
            RecordLog.warn("[ZookeeperDataSource] Error occurred when initializing Zookeeper data source", e);
            e.printStackTrace();
        }
    }

    private static void initClient(final String serverAddr, final List<AuthInfo> authInfos) {
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

    @Override
    public String readSource() throws Exception {
        if (zkClient == null) {
            throw new IllegalStateException("Zookeeper has not been initialized or error occurred");
        }
        String configInfo = null;
        ChildData childData = nodeCache.getCurrentData();
        if (null != childData && childData.getData() != null) {

            configInfo = new String(childData.getData());
        }
        return configInfo;
    }

    @Deprecated
    @Override
    public void close() throws Exception {
        synchronized (ZookeeperDataSource.class) {
            if (poolReferenceCount.get() == 1 && pool != null) {
                pool.shutdown();
                pool = null;
            } else {
                poolReferenceCount.decrementAndGet();
            }
        }
        if (this.nodeCache != null) {
            this.nodeCache.getListenable().removeListener(listener);
            this.nodeCache.close();
        }
        if (zkClient != null) {
            zkClient.close();
            zkClient = null;
        }
    }

    public static String getPath(String groupId, String dataId) {
        return String.format("/%s/%s", groupId, dataId);
    }


    // ---------------------------------------------------------------------------------------
    // Recommended: method chaining

    public static ZookeeperDataSourceBuilder builder() {
        return new ZookeeperDataSourceBuilder();
    }

    public static class ZookeeperDataSourceBuilder {
        private Map<Class, ZookeeperDataSource> clazzDataSourceMap = Maps.newConcurrentMap();
        private AtomicBoolean clientStarted = new AtomicBoolean(false);
        private String serverAddr;

        public ZookeeperDataSourceBuilder initClient(final String serverAddr, final List<AuthInfo> authInfos) {
            this.serverAddr = serverAddr;
            if (clientStarted.compareAndSet(false, true)) {
                if (zkClient == null) {
                    this.initClient(serverAddr, authInfos);
                } else {
                    throw new IllegalStateException(String.format("Bad state: please don't mix `new ZookeeperDataSource(...)` and this method"));
                }
            }
            return this;
        }

        public ZookeeperDataSourceBuilder initClient(final String serverAddr) {
            initClient(serverAddr, null);
            return this;
        }

        public <W> ZookeeperDataSourceBuilder register(String path, Class<W> clazz, Converter<String, List<W>> converter) {
            if (StringUtil.isBlank(serverAddr)) {
                throw new IllegalArgumentException(String.format("Bad argument: serverAddr=[%s]", serverAddr));
            }
            clazzDataSourceMap.put(clazz, new ZookeeperDataSource(serverAddr, path, converter));
            return this;
        }

        public <W> ZookeeperDataSourceBuilder register(final String groupId, final String dataId, Class<W> clazz, Converter<String, List<W>> converter) {
            if (StringUtil.isBlank(serverAddr)) {
                throw new IllegalArgumentException(String.format("Bad argument: serverAddr=[%s]", serverAddr));
            }
            clazzDataSourceMap.put(clazz, new ZookeeperDataSource(serverAddr, getPath(groupId, dataId), converter));
            return this;
        }

        public ZookeeperDataSourceBuilder build() throws Exception {
            List<ZookeeperDataSource> zookeeperDataSources = Lists.newArrayList();

            for (Map.Entry<Class, ZookeeperDataSource> entry : clazzDataSourceMap.entrySet()) {
                SentinelProperty property = entry.getValue().getProperty();
                if (entry.getKey().equals(FlowRule.class)) {
                    FlowRuleManager.register2Property(property);
                } else if (entry.getKey().equals(DegradeRule.class)) {
                    DegradeRuleManager.register2Property(property);
                } else if (entry.getKey().equals(SystemRule.class)) {
                    SystemRuleManager.register2Property(property);
                } else if (entry.getKey().equals(AuthorityRule.class)) {
                    AuthorityRuleManager.register2Property(property);
                } else if (entry.getKey().getName().equals("com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule")) {
                    Class<?> clazz = Class.forName("com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager");
                    Method register2Property = clazz.getDeclaredMethod("register2Property", SentinelProperty.class);
                    register2Property.invoke(null, property);
                } else {
                    throw new IllegalArgumentException(String.format("clazz from register() is not correct, clazz=[%s]", entry.getKey().getName()));
                }
                zookeeperDataSources.add(entry.getValue());
            }

            return this;
        }

        public void closeAll() throws IOException {
            if (pool != null) {
                pool.shutdown();
                pool = null;
            }
            for (ZookeeperDataSource dataSource : clazzDataSourceMap.values()) {
                if (dataSource.nodeCache != null) {
                    dataSource.nodeCache.getListenable().removeListener(dataSource.listener);
                    dataSource.nodeCache.close();
                }
            }
            if (zkClient != null) {
                zkClient.close();
                zkClient = null;
            }
        }
    }
}
