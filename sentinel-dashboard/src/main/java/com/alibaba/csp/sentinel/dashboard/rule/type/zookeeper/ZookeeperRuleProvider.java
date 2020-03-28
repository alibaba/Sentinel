package com.alibaba.csp.sentinel.dashboard.rule.type.zookeeper;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.AbstractRuleProvider;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

/**
 * @author cdfive
 */
@ConditionalOnBean(ZookeeperConfig.class)
@Component("zookeeperRuleProvider")
public class ZookeeperRuleProvider<T extends RuleEntity> extends AbstractRuleProvider<T> {

    @Autowired
    private CuratorFramework zkClient;

    @Override
    protected String fetchRules(String app, String ip, Integer port) throws Exception {
        String zkPath = buildRuleKey(app, ip, port);

        // Zookeeper's path must start with / character
        zkPath = "/" + zkPath;

        Stat stat = zkClient.checkExists().forPath(zkPath);
        if(stat == null){
            return null;
        }

        byte[] bytes = zkClient.getData().forPath(zkPath);
        if (ObjectUtils.isEmpty(bytes)) {
            return null;
        }

        return new String(bytes);
    }
}
