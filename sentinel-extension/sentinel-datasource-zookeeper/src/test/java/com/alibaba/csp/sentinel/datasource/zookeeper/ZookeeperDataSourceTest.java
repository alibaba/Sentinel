package com.alibaba.csp.sentinel.datasource.zookeeper;

import java.util.Collections;
import java.util.List;

import com.alibaba.csp.sentinel.datasource.ConfigParser;
import com.alibaba.csp.sentinel.datasource.DataSource;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * @author Eric Zhao
 */
public class ZookeeperDataSourceTest {

    @Test
    public void testZooKeeperDataSource() throws Exception {
        TestingServer server = new TestingServer(21812);
        server.start();

        final String remoteAddress = server.getConnectString();
        final String path = "/sentinel-zk-ds-demo/flow-HK";

        DataSource<String, List<FlowRule>> flowRuleDataSource = new ZookeeperDataSource<List<FlowRule>>(remoteAddress, path,
            new ConfigParser<String, List<FlowRule>>() {
                @Override
                public List<FlowRule> parse(String source) {
                    return JSON.parseObject(source, new TypeReference<List<FlowRule>>() {});
                }
            });
        FlowRuleManager.register2Property(flowRuleDataSource.getProperty());

        CuratorFramework zkClient = CuratorFrameworkFactory.newClient(remoteAddress,
            new ExponentialBackoffRetry(3, 1000));
        zkClient.start();
        Stat stat = zkClient.checkExists().forPath(path);
        if (stat == null) {
            zkClient.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path, null);
        }

        final String resourceName = "HK";
        publishThenTestFor(zkClient, path, resourceName, 10);
        publishThenTestFor(zkClient, path, resourceName, 15);

        zkClient.close();
        server.stop();
    }

    private void publishThenTestFor(CuratorFramework zkClient, String path, String resourceName, long count) throws Exception {
        FlowRule rule = new FlowRule().setResource(resourceName)
            .setLimitApp("default")
            .as(FlowRule.class)
            .setCount(count)
            .setGrade(RuleConstant.FLOW_GRADE_QPS);
        String ruleString = JSON.toJSONString(Collections.singletonList(rule));
        zkClient.setData().forPath(path, ruleString.getBytes());

        Thread.sleep(5000);

        List<FlowRule> rules = FlowRuleManager.getRules();
        assertTrue(rules != null && !rules.isEmpty());
        boolean exists = false;
        for (FlowRule r : rules) {
            if (resourceName.equals(r.getResource())) {
                exists = true;
                assertEquals(count, new Double(r.getCount()).longValue());
            }
        }
        assertTrue(exists);
    }
}