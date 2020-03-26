package com.alibaba.csp.sentinel.dashboard.rule.type.zookeeper;

import com.alibaba.csp.sentinel.dashboard.rule.AbstractRulePublisher;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

/**
 * @author cdfive
 */
@ConditionalOnBean(ZookeeperConfig.class)
@Component("zookeeperRulePublisher")
public class ZookeeperRulePublisher<T> extends AbstractRulePublisher<T> {

    @Autowired
    private CuratorFramework zkClient;

    @Override
    protected void publishRules(String app, String ip, Integer port, String rules) throws Exception {
        String zkPath = buildRuleKey(app, ip, port);

        // Zookeeper's path must start with / character
        zkPath = "/" + zkPath;

        Stat stat = zkClient.checkExists().forPath(zkPath);
        if (stat == null) {
            zkClient.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT).forPath(zkPath, null);
        }

        zkClient.setData().forPath(zkPath, rules.getBytes());
    }
}
