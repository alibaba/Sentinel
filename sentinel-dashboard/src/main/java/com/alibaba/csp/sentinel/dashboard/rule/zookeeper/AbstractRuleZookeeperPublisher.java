package com.alibaba.csp.sentinel.dashboard.rule.zookeeper;

import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.util.AssertUtil;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * 动态规则推送zk实现方式基类
 * @param <T> 需传入FlowRuleEntity、SystemRuleEntity、或者DegradeRuleEntity
 * @author lixiangqian
 */
public abstract class AbstractRuleZookeeperPublisher<T> implements DynamicRulePublisher<List<T>> {

    @Autowired
    private CuratorFramework zkClient;
    @Autowired
    private ZookeeperConfig zookeeperConfig;

    protected abstract String getType();

    protected abstract Converter<List<T>, String> getConverter();

    @Override
    public void publish(String app, List<T> rules) throws Exception {
        AssertUtil.notEmpty(app, "app name cannot be empty");

        String path = zookeeperConfig.getPath(app, getType());
        Stat stat = zkClient.checkExists().forPath(path);
        if (stat == null) {
            zkClient.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path, null);
        }
        byte[] data = CollectionUtils.isEmpty(rules) ? "[]".getBytes() : getConverter().convert(rules).getBytes();
        zkClient.setData().forPath(path, data);
    }
}
