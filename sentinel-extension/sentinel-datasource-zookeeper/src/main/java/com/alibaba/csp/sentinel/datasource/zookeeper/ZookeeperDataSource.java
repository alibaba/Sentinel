package com.alibaba.csp.sentinel.datasource.zookeeper;

import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.alibaba.csp.sentinel.datasource.AbstractDataSource;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.apache.curator.framework.AuthInfo;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * A read-only {@code DataSource} with ZooKeeper backend.
 *
 * @author guonanjun
 */
public class ZookeeperDataSource<T> extends AbstractDataSource<String, T> {

    private static final int RETRY_TIMES = 3;
    private static final int SLEEP_TIME = 1000;

    private static volatile Map<String, CuratorFramework> zkClientMap = new HashMap<>();
    private static final Object lock = new Object();


    private final ExecutorService pool = new ThreadPoolExecutor(1, 1, 0, TimeUnit.MILLISECONDS,
            new ArrayBlockingQueue<Runnable>(1), new NamedThreadFactory("sentinel-zookeeper-ds-update"),
            new ThreadPoolExecutor.DiscardOldestPolicy());

    private NodeCacheListener listener;
    private final String path;

    private CuratorFramework zkClient = null;
    private NodeCache nodeCache = null;

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

            this.listener = new NodeCacheListener() {
                @Override
                public void nodeChanged() {

                    try {
                        T newValue = loadConfig();
                        RecordLog.info("[ZookeeperDataSource] New property value received for ({}, {}): {}",
                            serverAddr, path, newValue);
                        // Update the new value to the property.
                        getProperty().updateValue(newValue);
                    } catch (Exception ex) {
                        RecordLog.warn("[ZookeeperDataSource] loadConfig exception", ex);
                    }
                }
            };

            String zkKey = getZkKey(serverAddr, authInfos);
            if (zkClientMap.containsKey(zkKey)) {
                this.zkClient = zkClientMap.get(zkKey);
            } else {
                synchronized (lock) {
                    if (!zkClientMap.containsKey(zkKey)) {
                        CuratorFramework zc = null;
                        if (authInfos == null || authInfos.size() == 0) {
                            zc = CuratorFrameworkFactory.newClient(serverAddr, new ExponentialBackoffRetry(SLEEP_TIME, RETRY_TIMES));
                        } else {
                            zc = CuratorFrameworkFactory.builder().
                                    connectString(serverAddr).
                                    retryPolicy(new ExponentialBackoffRetry(SLEEP_TIME, RETRY_TIMES)).
                                    authorization(authInfos).
                                    build();
                        }
                        this.zkClient = zc;
                        this.zkClient.start();
                        Map<String, CuratorFramework> newZkClientMap = new HashMap<>(zkClientMap.size());
                        newZkClientMap.putAll(zkClientMap);
                        newZkClientMap.put(zkKey, zc);
                        zkClientMap = newZkClientMap;
                    } else {
                        this.zkClient = zkClientMap.get(zkKey);
                    }
                }
            }

            this.nodeCache = new NodeCache(this.zkClient, this.path);
            this.nodeCache.getListenable().addListener(this.listener, this.pool);
            this.nodeCache.start();
        } catch (Exception e) {
            RecordLog.warn("[ZookeeperDataSource] Error occurred when initializing Zookeeper data source", e);
            e.printStackTrace();
        }
    }

    @Override
    public String readSource() throws Exception {
        if (this.zkClient == null) {
            throw new IllegalStateException("Zookeeper has not been initialized or error occurred");
        }
        String configInfo = null;
        ChildData childData = nodeCache.getCurrentData();
        if (null != childData && childData.getData() != null) {

            configInfo = new String(childData.getData());
        }
        return configInfo;
    }

    @Override
    public void close() throws Exception {
        if (this.nodeCache != null) {
            this.nodeCache.getListenable().removeListener(listener);
            this.nodeCache.close();
        }
        if (this.zkClient != null) {
            this.zkClient.close();
        }
        pool.shutdown();
    }

    private String getPath(String groupId, String dataId) {
        return String.format("/%s/%s", groupId, dataId);
    }

    private String getZkKey(final String serverAddr, final List<AuthInfo> authInfos) {
        if (authInfos == null || authInfos.size() == 0) {
            return serverAddr;
        }
        StringBuilder builder = new StringBuilder(64);
        builder.append(serverAddr).append(getAuthInfosKey(authInfos));
        return builder.toString();
    }

    private String getAuthInfosKey(List<AuthInfo> authInfos) {
        StringBuilder builder = new StringBuilder(32);
        for (AuthInfo authInfo : authInfos) {
            if (authInfo == null) {
                builder.append("{}");
            } else {
                builder.append("{" + "sc=" + authInfo.getScheme() + ",au=" + Arrays.toString(authInfo.getAuth()) + "}");
            }
        }
        return builder.toString();
    }

    protected CuratorFramework getZkClient() {
        return this.zkClient;
    }


}
