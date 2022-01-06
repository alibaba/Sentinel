package com.alibaba.csp.sentinel.dashboard.rule.zookeeper;

import com.alibaba.csp.sentinel.dashboard.rule.RuleConfigService;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.data.Stat;

public class ZookeeperConfigService implements RuleConfigService {
    private CuratorFramework zkClient;

    public ZookeeperConfigService(CuratorFramework zkClient) {
        this.zkClient = zkClient;
    }

    @Override
    public String getConfig(String dataId, String group) {
        String zkPath = ZookeeperConfigUtil.getPath(dataId,group);
        try {
            Stat stat = zkClient.checkExists().forPath(zkPath);
            if(stat == null){
                return null;
            }
            byte[] bytes = zkClient.getData().forPath(zkPath);
            if (null == bytes || bytes.length == 0) {
                return null;
            }
            return new String(bytes);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void publishConfig(String dataId, String group, String content) {

    }
}
