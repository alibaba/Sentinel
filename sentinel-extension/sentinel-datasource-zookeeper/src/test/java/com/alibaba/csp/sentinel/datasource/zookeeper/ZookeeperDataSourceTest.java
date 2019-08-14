package com.alibaba.csp.sentinel.datasource.zookeeper;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.alibaba.csp.sentinel.datasource.ReadableDataSource;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
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
import org.junit.Assert;
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
    public void testZooKeeperDataSource() throws Exception {
        TestingServer server = new TestingServer(21812);
        server.start();

        final String remoteAddress = server.getConnectString();
        final String path = "/sentinel-zk-ds-demo/flow-HK";

        ReadableDataSource<String, List<FlowRule>> flowRuleDataSource = new ZookeeperDataSource<List<FlowRule>>(remoteAddress, path,
                new Converter<String, List<FlowRule>>() {
                    @Override
                    public List<FlowRule> convert(String source) {
                        return JSON.parseObject(source, new TypeReference<List<FlowRule>>() {
                        });
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

        CuratorFramework zkClient = CuratorFrameworkFactory.builder().
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

        ReadableDataSource<String, List<FlowRule>> flowRuleDataSource = new ZookeeperDataSource<List<FlowRule>>(remoteAddress,
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


    /**
     * Test whether different dataSources can share the same zkClient when the connection parameters are the same.
     * @throws Exception
     */
    @Test
    public void testZooKeeperDataSourceSameZkClient() throws Exception {
        TestingServer server = new TestingServer(21813);
        server.start();

        final String remoteAddress = server.getConnectString();
        final String flowPath = "/sentinel-zk-ds-demo/flow-HK";
        final String degradePath = "/sentinel-zk-ds-demo/degrade-HK";


        ZookeeperDataSource<List<FlowRule>> flowRuleZkDataSource = new ZookeeperDataSource<>(remoteAddress, flowPath,
                new Converter<String, List<FlowRule>>() {
                    @Override
                    public List<FlowRule> convert(String source) {
                        return JSON.parseObject(source, new TypeReference<List<FlowRule>>() {
                        });
                    }
                });
        ZookeeperDataSource<List<DegradeRule>> degradeRuleZkDataSource = new ZookeeperDataSource<>(remoteAddress, degradePath,
                new Converter<String, List<DegradeRule>>() {
                    @Override
                    public List<DegradeRule> convert(String source) {
                        return JSON.parseObject(source, new TypeReference<List<DegradeRule>>() {
                        });
                    }
                });


        Assert.assertTrue(flowRuleZkDataSource.getZkClient() == degradeRuleZkDataSource.getZkClient());


        final String groupId = "sentinel-zk-ds-demo";
        final String flowDataId = "flow-HK";
        final String degradeDataId = "degrade-HK";
        final String scheme = "digest";
        final String auth = "root:123456";
        AuthInfo authInfo = new AuthInfo(scheme, auth.getBytes());
        List<AuthInfo> authInfoList = Collections.singletonList(authInfo);


        ZookeeperDataSource<List<FlowRule>> flowRuleZkAutoDataSource = new ZookeeperDataSource<List<FlowRule>>(remoteAddress,
                authInfoList, groupId, flowDataId,
                new Converter<String, List<FlowRule>>() {
                    @Override
                    public List<FlowRule> convert(String source) {
                        return JSON.parseObject(source, new TypeReference<List<FlowRule>>() {
                        });
                    }
                });

        ZookeeperDataSource<List<DegradeRule>> degradeRuleZkAutoDataSource = new ZookeeperDataSource<List<DegradeRule>>(remoteAddress,
                authInfoList, groupId, degradeDataId,
                new Converter<String, List<DegradeRule>>() {
                    @Override
                    public List<DegradeRule> convert(String source) {
                        return JSON.parseObject(source, new TypeReference<List<DegradeRule>>() {
                        });
                    }
                });

        Assert.assertTrue(flowRuleZkAutoDataSource.getZkClient() == degradeRuleZkAutoDataSource.getZkClient());

        server.stop();
    }



}