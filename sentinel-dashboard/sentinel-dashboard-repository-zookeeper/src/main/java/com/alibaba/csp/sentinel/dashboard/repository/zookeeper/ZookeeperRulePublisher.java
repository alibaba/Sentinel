package com.alibaba.csp.sentinel.dashboard.repository.zookeeper;

import com.alibaba.csp.sentinel.dashboard.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.repository.AbstractRulePublisher;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author cdfive
 */
//@ConditionalOnBean(ZookeeperConfig.class)
//@Component("zookeeperRulePublisher")
public class ZookeeperRulePublisher<T extends RuleEntity> extends AbstractRulePublisher<T> {

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
