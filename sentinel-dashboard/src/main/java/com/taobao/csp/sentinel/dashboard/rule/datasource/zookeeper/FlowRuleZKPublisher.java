package com.taobao.csp.sentinel.dashboard.rule.datasource.zookeeper;

import com.alibaba.csp.sentinel.datasource.Converter;
import com.taobao.csp.sentinel.dashboard.datasource.entity.rule.DegradeRuleEntity;
import com.taobao.csp.sentinel.dashboard.datasource.entity.rule.FlowRuleEntity;
import com.taobao.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 类名称：FlowRuleZKPublisher
 * 类描述：发布ZK更新
 * 开发人：朱水平【Tank】
 * 创建时间：2018/12/20.20:49
 * 修改备注：
 *
 * @version 1.0.0
 */
@Component("flowRuleZKPublisher")
public class FlowRuleZKPublisher implements DynamicRulePublisher<List<FlowRuleEntity>> {
    @Value("${zookeeper.remoteAddress}")
    private String remoteAddress;
    @Autowired
    private Converter<List<FlowRuleEntity>, String> converter;

    @Autowired
    private Converter<List<DegradeRuleEntity>, String> degradeConverter;

    /**
     * 限流规则持久化到ZK
     *
     * @param app   app name
     * @param rules list of rules to push
     * @throws Exception
     */
    @Override
    public void publish(String app, List<FlowRuleEntity> rules) throws Exception {
        CuratorFramework zkClient = null;
        try {
            zkClient = CuratorFrameworkFactory.newClient(remoteAddress,
                    new ExponentialBackoffRetry(1000, 3));
            zkClient.start();
            String path = ZookeeperConfigUtil.getPath(app, ZookeeperConfigUtil.FLOW_RULES);
            Stat stat = zkClient.checkExists().forPath(path);
            if (stat == null) {
                zkClient.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path, null);
            }
            zkClient.setData().forPath(path, converter.convert(rules).getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (zkClient != null) {
                zkClient.close();
            }
        }
    }

    public boolean publishDegradeRuleOfMachine(String app, List<DegradeRuleEntity> rules){
        CuratorFramework zkClient = null;
        try {
            zkClient = CuratorFrameworkFactory.newClient(remoteAddress,
                    new ExponentialBackoffRetry(1000, 3));
            zkClient.start();
            String path = ZookeeperConfigUtil.getPath(app, ZookeeperConfigUtil.DEGRADE_RULES);
            Stat stat = zkClient.checkExists().forPath(path);
            if (stat == null) {
                zkClient.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path, null);
            }
            zkClient.setData().forPath(path, degradeConverter.convert(rules).getBytes());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (zkClient != null) {
                zkClient.close();
            }
        }
        return false;
    }

    public void publishClusterModifyRequest(String app,String ip ,String data){
        if (StringUtils.isBlank(data)){
            return ;
        }
        CuratorFramework zkClient = null;
        try {
            zkClient = CuratorFrameworkFactory.newClient(remoteAddress,
                    new ExponentialBackoffRetry(1000, 3));
            zkClient.start();
            String path = ZookeeperConfigUtil.getPath(app,ip);
            Stat stat = zkClient.checkExists().forPath(path);
            if (stat == null) {
                zkClient.create().creatingParentContainersIfNeeded().withMode(CreateMode.PERSISTENT).forPath(path, null);
            }
            zkClient.setData().forPath(path,data.getBytes());
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (zkClient != null) {
                zkClient.close();
            }
        }
    }
}
