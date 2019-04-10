package com.alibaba.csp.sentinel.datasource.zookeeper;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowItem;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import org.apache.curator.framework.AuthInfo;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.test.TestingServer;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.data.Stat;
import org.apache.zookeeper.server.auth.DigestAuthenticationProvider;
import org.junit.Test;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author Eric Zhao
 */
public class ZookeeperDataSourceTest {
    @Test
    public void testZooKeeperAllDataSource() throws Exception {
        TestingServer server = new TestingServer(21812);
        server.start();

        final String remoteAddress = server.getConnectString();
        final String path1 = "/sentinel-zk-ds-demo/flow-HK";
        final String path2 = "/sentinel-zk-ds-demo/param-HK";

        CuratorFramework zkClient = null;
        ZookeeperDataSource.ZookeeperDataSourceBuilder zookeeperDataSourceBuilder = null;

        try {
            zookeeperDataSourceBuilder = ZookeeperDataSource.builder()
                    .initClient(remoteAddress)
                    .register(path1, FlowRule.class, new Converter<String, List<FlowRule>>() {
                        @Override
                        public List<FlowRule> convert(String source) {
                            return JSON.parseObject(source, new TypeReference<List<FlowRule>>() {});
                        }
                    })
                    .register(path2, ParamFlowRule.class, new Converter<String, List<ParamFlowRule>>() {
                        @Override
                        public List<ParamFlowRule> convert(String source) {
                            return JSON.parseObject(source, new TypeReference<List<ParamFlowRule>>() {});
                        }
                    })
                    .build();

            zkClient = CuratorFrameworkFactory.newClient(remoteAddress,
                    new ExponentialBackoffRetry(3, 1000));
            zkClient.start();
            Stat stat = zkClient.checkExists().forPath(path1);
            if (stat == null) {
                zkClient.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path1, null);
            }
            stat = zkClient.checkExists().forPath(path2);
            if (stat == null) {
                zkClient.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path2, null);
            }

            final String resourceName = "HK";
            publishThenTestFor(zkClient, path1, resourceName, 10);
            publishThenTestFor(zkClient, path1, resourceName, 15);

            publishParamFlowRuleThenTestFor(zkClient, path2, resourceName, 20);
            publishParamFlowRuleThenTestFor(zkClient, path2, resourceName, 22);
        } finally {
            if (zookeeperDataSourceBuilder != null) {
                zookeeperDataSourceBuilder.closeAll();
            }
            if (zkClient != null) {
                zkClient.close();
            }
            server.stop();
        }
    }

    @Test
    public void testZooKeeperDataSource() throws Exception {
        TestingServer server = new TestingServer(21812);
        server.start();

        final String remoteAddress = server.getConnectString();
        final String path = "/sentinel-zk-ds-demo/flow-HK";

        CuratorFramework zkClient = null;
        ReadableDataSource<String, List<FlowRule>> flowRuleDataSource = null;

        try {
            flowRuleDataSource = new ZookeeperDataSource<List<FlowRule>>(remoteAddress, path,
                    new Converter<String, List<FlowRule>>() {
                        @Override
                        public List<FlowRule> convert(String source) {
                            return JSON.parseObject(source, new TypeReference<List<FlowRule>>() {});
                        }
                    });
            FlowRuleManager.register2Property(flowRuleDataSource.getProperty());

            zkClient = CuratorFrameworkFactory.newClient(remoteAddress,
                    new ExponentialBackoffRetry(3, 1000));
            zkClient.start();
            Stat stat = zkClient.checkExists().forPath(path);
            if (stat == null) {
                zkClient.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path, null);
            }

            final String resourceName = "HK";
            publishThenTestFor(zkClient, path, resourceName, 10);
            publishThenTestFor(zkClient, path, resourceName, 15);
        } finally {
            if (flowRuleDataSource != null) {
                flowRuleDataSource.close();
            }
            if (zkClient != null) {
                zkClient.close();
            }
            server.stop();
        }
    }

    @Test
    public void testZooKeeperDataSourceAuthorization() throws Exception {
        TestingServer server = new TestingServer(21812);
        server.start();

        final String remoteAddress = server.getConnectString();
        final String groupId = "sentinel-zk-ds-demo";
        final String dataId = "flow-HK";
        final String path = "/" + groupId + "/" + dataId;
        final String scheme = "digest";
        final String auth = "root:123456";

        AuthInfo authInfo = new AuthInfo(scheme, auth.getBytes());
        List<AuthInfo> authInfoList = Collections.singletonList(authInfo);

        CuratorFramework zkClient = null;
        ReadableDataSource<String, List<FlowRule>> flowRuleDataSource = null;

        try {
            zkClient = CuratorFrameworkFactory.builder().
                    connectString(remoteAddress).
                    retryPolicy(new ExponentialBackoffRetry(3, 100)).
                    authorization(authInfoList).
                    build();
            zkClient.start();
            Stat stat = zkClient.checkExists().forPath(path);
            if (stat == null) {
                ACL acl = new ACL(ZooDefs.Perms.ALL, new Id(scheme, DigestAuthenticationProvider.generateDigest(auth)));
                zkClient.create().creatingParentContainersIfNeeded().withACL(Collections.singletonList(acl)).forPath(path, null);
            }

            flowRuleDataSource = new ZookeeperDataSource<List<FlowRule>>(remoteAddress,
                    authInfoList, groupId, dataId,
                    new Converter<String, List<FlowRule>>() {
                        @Override
                        public List<FlowRule> convert(String source) {
                            return JSON.parseObject(source, new TypeReference<List<FlowRule>>() {
                            });
                        }
                    });
            FlowRuleManager.register2Property(flowRuleDataSource.getProperty());


            final String resourceName = "HK";
            publishThenTestFor(zkClient, path, resourceName, 10);
            publishThenTestFor(zkClient, path, resourceName, 15);
        } finally {
            if (flowRuleDataSource != null) {
                flowRuleDataSource.close();
            }
            if (zkClient != null) {
                zkClient.close();
            }
            server.stop();
        }
    }

    private void publishThenTestFor(CuratorFramework zkClient, String path, String resourceName, long count) throws Exception {
        FlowRule rule = new FlowRule().setResource(resourceName)
            .setLimitApp("default")
            .as(FlowRule.class)
            .setCount(count)
            .setGrade(RuleConstant.FLOW_GRADE_QPS);
        String ruleString = JSON.toJSONString(Collections.singletonList(rule));
        zkClient.setData().forPath(path, ruleString.getBytes());

        await().timeout(5, TimeUnit.SECONDS)
            .until(new Callable<Boolean>() {
                @Override
                public Boolean call() throws Exception {
                    List<FlowRule> rules = FlowRuleManager.getRules();
                    return rules != null && !rules.isEmpty();
                }
            });

        List<FlowRule> rules = FlowRuleManager.getRules();
        boolean exists = false;
        for (FlowRule r : rules) {
            if (resourceName.equals(r.getResource())) {
                exists = true;
                assertEquals(count, new Double(r.getCount()).longValue());
            }
        }
        assertTrue(exists);
    }

    private void publishParamFlowRuleThenTestFor(CuratorFramework zkClient, String path, String resourceName, long count) throws Exception {
        ParamFlowRule rule = new ParamFlowRule(resourceName)
                .setParamIdx(0)
                .setGrade(RuleConstant.FLOW_GRADE_QPS)
                .setCount(count);
        ParamFlowItem item = new ParamFlowItem().setObject("2")
                .setClassType(int.class.getName())
                .setCount(3);
        rule.setParamFlowItemList(Collections.singletonList(item));

        String ruleString = JSON.toJSONString(Collections.singletonList(rule));
        zkClient.setData().forPath(path, ruleString.getBytes());

        await().timeout(5, TimeUnit.SECONDS)
                .until(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        List<ParamFlowRule> rules = ParamFlowRuleManager.getRules();
                        return rules != null && !rules.isEmpty();
                    }
                });

        List<ParamFlowRule> rules = ParamFlowRuleManager.getRules();
        boolean exists = false;
        for (ParamFlowRule r : rules) {
            if (resourceName.equals(r.getResource())) {
                exists = true;
                assertEquals(count, new Double(r.getCount()).longValue());
            }
        }
        assertTrue(exists);
    }
}