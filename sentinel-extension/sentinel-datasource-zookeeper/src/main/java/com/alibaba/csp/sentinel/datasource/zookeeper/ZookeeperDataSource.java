package com.alibaba.csp.sentinel.datasource.zookeeper;

import com.alibaba.csp.sentinel.datasource.AbstractDataSource;
import com.alibaba.csp.sentinel.datasource.ConfigParser;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.StringUtil;
import org.I0Itec.zkclient.IZkDataListener;
import org.I0Itec.zkclient.ZkClient;

/**
 * Zookeeper DataSource
 *
 * @author guonanjun
 */
public class ZookeeperDataSource<T> extends AbstractDataSource<String, T> {

    private static final int DEFAULT_TIMEOUT = 3000;

    private final IZkDataListener zkDataListener;
    private final String groupId;
    private final String dataId;

    private ZkClient zkClient = null;

    public ZookeeperDataSource(final String serverAddr, final String groupId, final String dataId,
                               ConfigParser<String, T> parser) {
        super(parser);
        if (StringUtil.isBlank(serverAddr) || StringUtil.isBlank(groupId) || StringUtil.isBlank(dataId)) {
            throw new IllegalArgumentException(String.format("Bad argument: serverAddr=[%s], groupId=[%s], dataId=[%s]",
                    serverAddr, groupId, dataId));
        }
        this.groupId = groupId;
        this.dataId = dataId;
        this.zkDataListener = new IZkDataListener() {
            @Override
            public void handleDataChange(String path, Object configInfo) throws Exception {
                RecordLog.info(String.format("[ZookeeperDataSource] New property value received for (%s, %s, %s): %s",
                        serverAddr, dataId, groupId, configInfo));
                System.out.println("===========" + configInfo);
                T newValue = ZookeeperDataSource.this.parser.parse(String.valueOf(configInfo));
                // Update the new value to the property.
                getProperty().updateValue(newValue);
            }

            @Override
            public void handleDataDeleted(String s) throws Exception {
                RecordLog.info(String.format("[ZookeeperDataSource] New property value received for (%s, %s, %s): %s",
                        serverAddr, dataId, groupId, null));
                // Update the new value to the property.
                getProperty().updateValue(null);
            }
        };
        initZookeeperListener(serverAddr);
        loadInitialConfig();
    }

    private void loadInitialConfig() {
        try {
            T newValue = loadConfig();
            if (newValue == null) {
                RecordLog.info("[ZookeeperDataSource] WARN: initial config is null, you may have to check your data source");
            }
            getProperty().updateValue(newValue);
        } catch (Exception ex) {
            RecordLog.info("[ZookeeperDataSource] Error when loading initial config", ex);
        }
    }

    private void initZookeeperListener(String serverAddr) {
        try {
            this.zkClient = new ZkClient(serverAddr, DEFAULT_TIMEOUT);
            String path = "/" + this.groupId + "/" + this.dataId;
            if (!zkClient.exists(path)) {
                zkClient.createPersistent(path, true);
            }
            zkClient.subscribeDataChanges(path, zkDataListener);
        } catch (Exception e) {
            RecordLog.info("[ZookeeperDataSource] Error occurred when initializing Zookeeper data source", e);
            e.printStackTrace();
        }
    }

    @Override
    public String readSource() throws Exception {
        if (zkClient == null) {
            throw new IllegalStateException("Zookeeper has not been initialized or error occurred");
        }
        String path = "/" + this.groupId + "/" + this.dataId;
        return zkClient.readData(path);
    }

    @Override
    public void close() throws Exception {
        if (zkClient != null) {
            String path = "/" + this.groupId + "/" + this.dataId;
            zkClient.unsubscribeDataChanges(path, zkDataListener);
        }
        zkClient.close();
    }
}
